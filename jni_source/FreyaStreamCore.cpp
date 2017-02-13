#include <string.h>
#include <time.h>
#include "FreyaStreamCore.h"
#include "include/libyuv.h"

char rtmp_address_str[256] = {0};
char h264profile_str[256] = {0};
char h264encodemode_str[256] = {0};
char aacprofile_str[256] = {0};
int sample_rate_value = 0;
int dest_width_value = 1280;
int dest_height_value = 720;


FreyaStreamCore *pthis;


static long long TSCalculate()
{
	struct timeval tsvalue;
	gettimeofday(&tsvalue, NULL);
	long long tsvalue_present = (tsvalue.tv_sec * 1000) + (tsvalue.tv_usec / 1000);
	return tsvalue_present;
}


FreyaStreamCore::FreyaStreamCore()
{
	pthis = this;
	
	av_context = NULL;

	audio_stream = NULL;
	video_stream = NULL;

	audio_pts_value = 0;
	video_pts_value = 0;

	video_codec = NULL;
	audio_codec = NULL;

	video_context = NULL;
	audio_context = NULL;

	video_out_buffer = NULL;
	audio_out_buffer = NULL;

	audio_frame_buffer = NULL;

	video_out_buffer_size = 0;
	audio_out_buffer_size = 0;
	audio_input_frame_size = 0;
	audio_data_size_value = 0;

	video_frame = NULL;
	audio_frame = NULL;

	is_video_ready_flag = 0;
	is_audio_ready_flag = 0;
	is_session_start_flag = 0;
		
	video_fps_value = 0;
	video_bitrate_value = 0;
	
	initialTSvalue = 0;
	
	pthread_mutex_init(&video_lock,NULL);
	pthread_mutex_init(&audio_lock,NULL);
	pthread_mutex_init(&write_lock,NULL);

}
FreyaStreamCore::~FreyaStreamCore()
{

}

void FreyaStreamCore::Initialize()
{
	av_register_all();
	avformat_network_init();

	return ;
}
void FreyaStreamCore::Destroy()
{
	stopSession();

	pthread_mutex_destroy(&video_lock);
	pthread_mutex_destroy(&audio_lock);
	pthread_mutex_destroy(&write_lock);

	return ;
}

int FreyaStreamCore::setupRtmpUrl(const char* url)
{
	memset(rtmp_address_str,'\0',256);

	memcpy(rtmp_address_str,url,strlen(url));

	if (av_context!=NULL)
	{
		avio_close(av_context->pb);
		avformat_free_context(av_context);
	}

	int ret = avformat_alloc_output_context2(&av_context, NULL, "flv", rtmp_address_str);
	if( ret < 0){ 
		return -1; 
	}
	
	return 0;

}

int FreyaStreamCore::setupVideoParameter(int width, int height,int bitrate, int fps, const char* h264profile, const char* h264encodemode)
{
	if (!av_context)
	{
		return -1;
	}
	
	CloseVideoCode();

	pthread_mutex_lock(&video_lock);
	
	memset(h264profile_str,'\0',256);
    memcpy(h264profile_str,h264profile,strlen(h264profile));

	memset(h264encodemode_str,'\0',256);
    memcpy(h264encodemode_str,h264encodemode,strlen(h264encodemode));
	
	video_fps_value = fps;
	video_bitrate_value = bitrate;

	dest_width_value = width;
	dest_height_value = height;

	if(video_codec == NULL)
		video_codec = avcodec_find_encoder(AV_CODEC_ID_H264);

	if (video_codec != NULL){
		av_context->oformat->video_codec = AV_CODEC_ID_H264;
	} else {
		pthread_mutex_unlock(&video_lock);
		return -2;
	}
	
	AVRational videoRate = av_d2q(fps, 100000);

	if(video_stream == NULL)
	{
		video_stream = avformat_new_stream(av_context, video_codec);

		if (video_stream == NULL) 
		{
			pthread_mutex_unlock(&video_lock);
			return -4;
		}
	}

	video_context = video_stream->codec;
	video_context->codec_id = AV_CODEC_ID_H264;
	video_context->codec_type = AVMEDIA_TYPE_VIDEO;

	video_context->bit_rate = bitrate;
	video_context->rc_max_rate = bitrate;
	video_context->rc_min_rate = bitrate;
	video_context->rc_buffer_size = bitrate/2;
    video_context->bit_rate_tolerance = bitrate;
    video_context->rc_initial_buffer_occupancy = video_context->rc_buffer_size*3/4;
    video_context->rc_buffer_aggressivity= (float)1.0;
    video_context->rc_initial_cplx= 0.5; 
	
	video_context->width = width;
	video_context->height = height;

	video_context->time_base = av_inv_q(videoRate);
	video_stream->time_base = av_inv_q(videoRate);
	video_context->gop_size = fps;
	video_context->pix_fmt = AV_PIX_FMT_YUV420P;
	video_context->delay = 0;
	video_context->max_b_frames = 0;

	if ((av_context->oformat->flags & AVFMT_GLOBALHEADER) != 0) {
		video_context->flags = video_context->flags | CODEC_FLAG_GLOBAL_HEADER;
	}

	if ((video_codec->capabilities & CODEC_CAP_EXPERIMENTAL) != 0) {
		video_context->strict_std_compliance = FF_COMPLIANCE_EXPERIMENTAL;
	}

	AVDictionary *options = NULL;
	av_dict_set(&options, "profile", h264profile_str, 0);
	av_dict_set(&options, "preset", h264encodemode_str, 0);
	av_dict_set(&options, "tune", "zerolatency", 0);

	if (avcodec_open2(video_context, video_codec, &options) < 0) {
		pthread_mutex_unlock(&video_lock);
		return -5;
	}

	av_dict_free(&options);

	if(video_out_buffer != NULL)
	{
		av_free(video_out_buffer);
		video_out_buffer = NULL;
	}

	if ((av_context->oformat->flags & AVFMT_RAWPICTURE) == 0) 
	{
		video_out_buffer_size = avpicture_get_size(video_context->pix_fmt, 
												video_context->width,
												video_context->height);

		video_out_buffer = (uint8_t*)av_malloc(video_out_buffer_size);
	}

	if(video_frame == NULL)
	{
		video_frame = av_frame_alloc();

		if (video_frame == NULL)
		{
			pthread_mutex_unlock(&video_lock);
			return -6;
		}

		video_frame->pts = 0;
	}

	AVDictionary *metadata1 = NULL;
	video_stream->metadata = metadata1;

	is_video_ready_flag = 1;

	pthread_mutex_unlock(&video_lock);

	return 0;
}

int FreyaStreamCore::setupAudioParameter(int sample_rate, int channels, int soundrate, const char* aacprofile)
{
	
   CloseAudioCode();

	pthread_mutex_lock(&audio_lock);
	
	sample_rate_value = sample_rate;
	memset(aacprofile_str,'\0',256);
    memcpy(aacprofile_str,aacprofile,strlen(aacprofile));
	
	
	audio_codec = avcodec_find_encoder_by_name("libfdk_aac");
	if (audio_codec != NULL) {
		av_context->oformat->audio_codec = AV_CODEC_ID_AAC;
		audio_codec->capabilities |= AV_CODEC_CAP_VARIABLE_FRAME_SIZE;
	} else {
		pthread_mutex_unlock(&audio_lock);
		return -3;
	}
	
	if ((audio_stream = avformat_new_stream(av_context, audio_codec)) == NULL) {
		pthread_mutex_unlock(&audio_lock);
		return -5;
	}

	audio_context = audio_stream->codec;
	audio_context->codec_id = av_context->oformat->audio_codec;
	audio_context->codec_type = AVMEDIA_TYPE_AUDIO;
	audio_context->bit_rate = soundrate;
	audio_context->sample_rate = sample_rate;


	audio_context->time_base.num = 1;
	audio_context->time_base.den = sample_rate;
	audio_stream->time_base.num = 1;
	audio_stream->time_base.den = sample_rate;
	audio_context->channels = channels;
	audio_context->channel_layout = av_get_default_channel_layout(audio_context->channels);

	audio_context->sample_fmt = AV_SAMPLE_FMT_S16;
	

	audio_context->bits_per_raw_sample = 16;
	if ((av_context->oformat->flags & AVFMT_GLOBALHEADER) != 0) {
		audio_context->flags = audio_context->flags | CODEC_FLAG_GLOBAL_HEADER;
	}
	if ((audio_codec->capabilities & CODEC_CAP_EXPERIMENTAL) != 0) {
		audio_context->strict_std_compliance = FF_COMPLIANCE_EXPERIMENTAL;
	}

	AVDictionary *options = NULL;
		
	if (strcmp(aacprofile_str, "aac_lc") != 0){
	    av_dict_set(&options, "profile", aacprofile_str, 0);
	}
	
	int ret = avcodec_open2(audio_context, audio_codec, &options);
	if ( ret < 0) {
		pthread_mutex_unlock(&audio_lock);
		return -8;
	}
	av_dict_free(&options);

	audio_out_buffer_size = 512 * 1024;
	audio_out_buffer = (uint8_t *)av_malloc(audio_out_buffer_size);
	
	if (audio_context->frame_size <= 1) {

		audio_out_buffer_size = FF_MIN_BUFFER_SIZE;
		audio_input_frame_size = audio_out_buffer_size / audio_context->channels;
		if (audio_context->codec_id == AV_CODEC_ID_PCM_U16BE) {
			audio_input_frame_size >>= 1;
		}
	} else {
		audio_input_frame_size = audio_context->frame_size;
	}

	
	int planes = 1;
	audio_data_size_value = av_samples_get_buffer_size(NULL, audio_context->channels,
		audio_input_frame_size, audio_context->sample_fmt, 1) / planes;

	audio_frame = av_frame_alloc();
	audio_frame->pts = 0;

	audio_frame->nb_samples = 1024;
	
	audio_frame->format = audio_context->sample_fmt;

	audio_frame_buffer = (uint8_t *) av_malloc(audio_data_size_value);
	

	if (ret < 0) {
		pthread_mutex_unlock(&audio_lock);
		return -2;
	}

	av_init_packet(&audio_packet);
	AVDictionary *metadata2 = NULL;
	audio_stream->metadata = metadata2;

	is_audio_ready_flag = 1;

	pthread_mutex_unlock(&audio_lock);
	
	return 0;
}

int FreyaStreamCore::startSession()
{
	if(av_context==NULL)
	{
		return -22;
	}
	int ret = -1;

	
	ret = avio_open2(&av_context->pb, rtmp_address_str, AVIO_FLAG_READ_WRITE | AVIO_FLAG_NONBLOCK , NULL, NULL);
	
	if(ret < 0){ 
		return -1; 
	} 
	else{
	}
	
	strcpy(av_context->filename, rtmp_address_str);
	av_context->max_interleave_delta = 1000000/2;

	
	ret = avformat_write_header(av_context, NULL);
	
	if (ret < 0) {
		return -3;
	}

	initialTSvalue = TSCalculate();
	
	is_session_start_flag = 1;

	return 0;
}
int FreyaStreamCore::stopSession()
{	
	is_session_start_flag = 0;
	
	usleep(500);

	CloseVideoCode();

	CloseAudioCode();

	if (av_context!=NULL)
	{
		if(av_context->pb!=NULL)
		{
			avio_close(av_context->pb);
			av_context->pb = NULL;
		}

		avformat_free_context(av_context);
		av_context = NULL;
	}


	av_context = NULL;

	audio_stream = NULL;
	video_stream = NULL;

	audio_pts_value = 0;
	video_pts_value = 0;

	video_codec = NULL;
	audio_codec = NULL;

	video_context = NULL;
	audio_context = NULL;

	video_out_buffer = NULL;
	audio_out_buffer = NULL;

	audio_frame_buffer = NULL;

	video_out_buffer_size = 0;
	audio_out_buffer_size = 0;
	audio_input_frame_size = 0;
	audio_data_size_value = 0;

	video_frame = NULL;
	audio_frame = NULL;

	is_video_ready_flag = 0;
	is_audio_ready_flag = 0;
	is_session_start_flag = 0;
		
	video_fps_value = 0;
	video_bitrate_value = 0;
	
	initialTSvalue = 0;
	
	return 0;
}

void FreyaStreamCore::CloseVideoCode()
{
	pthread_mutex_lock(&video_lock);

	if (video_context!=NULL)
	{
		avcodec_close(video_context);
		video_context = NULL;
	}
	
	if(video_out_buffer != NULL)
	{
		av_free(video_out_buffer);
		video_out_buffer = NULL;
		video_out_buffer_size = 0;
	}
	
	
	is_video_ready_flag = 0;

	pthread_mutex_unlock(&video_lock);
}

void FreyaStreamCore::CloseAudioCode()
{

	pthread_mutex_lock(&audio_lock);

	audio_stream = NULL;
	
	if (audio_context!=NULL)
	{
		avcodec_close(audio_context);
		audio_context = NULL;
	}

	is_audio_ready_flag = 0;

	pthread_mutex_unlock(&audio_lock);
}

int FreyaStreamCore::VideoPusher(unsigned char * data, unsigned int len, int in_width, int in_height, long ts, int is_front, int orientation)
{
	if (is_video_ready_flag == 0 || is_session_start_flag == 0){
		av_free_packet(&video_packet);
		video_codec = NULL;
        video_stream = NULL;
        return -1;
	}

	
	pthread_mutex_lock(&video_lock);

	int ret              = 0;
	int got_video_packet = 0;
    int rotate_width     = in_height;		
    int rotate_height    = in_width;
	int dst_width        = dest_width_value;
	int dst_height       = dest_height_value;
	int yuv_size         = in_width * in_height * 3 / 2;
	uint8_t* yuv_i420    = (uint8_t *) malloc(yuv_size);

	int Ysize = in_width * in_height;
	size_t src_size = Ysize * 1.5;
    unsigned char* I420 = new unsigned char[len];
	
	if(is_front){
        unsigned char* pDstY = I420;
        unsigned char* pDstU = I420 + Ysize;
        unsigned char* pDstV = pDstU + (Ysize / 4);
		
		
	    int retVal = 0;

		if (orientation==90){
		    libyuv::RotationMode mode = libyuv::kRotate0;
		    retVal = libyuv::ConvertToI420(data, src_size, pDstY, in_width, pDstU, in_width / 2, pDstV, in_width / 2, 0, 0, in_width, in_height, in_width, in_height, mode, libyuv::FOURCC_NV21);
		}
		else{
			libyuv::RotationMode mode = libyuv::kRotate270;
		    retVal = libyuv::ConvertToI420(data, src_size, pDstY, in_height, pDstU, in_height / 2, pDstV, in_height / 2, 0, 0, in_width, in_height, in_width, in_height, mode, libyuv::FOURCC_NV21);
		}
		yuv_i420 = (uint8_t *)I420;
	}
	else{
        unsigned char* pDstY = I420;
        unsigned char* pDstU = I420 + Ysize;
        unsigned char* pDstV = pDstU + (Ysize / 4);
	    int retVal = 0;

		if (orientation==90){
		    libyuv::RotationMode mode = libyuv::kRotate0;
		    retVal = libyuv::ConvertToI420(data, src_size, pDstY, in_width, pDstU, in_width / 2, pDstV, in_width / 2, 0, 0, in_width, in_height, in_width, in_height, mode, libyuv::FOURCC_NV21);
		}
		else{
			libyuv::RotationMode mode = libyuv::kRotate90;
		    retVal = libyuv::ConvertToI420(data, src_size, pDstY, in_height, pDstU, in_height / 2, pDstV, in_height / 2, 0, 0, in_width, in_height, in_width, in_height, mode, libyuv::FOURCC_NV21);
		}
		yuv_i420 = (uint8_t *)I420;
	}
	
    ret = avpicture_fill((AVPicture *) video_frame, yuv_i420, AV_PIX_FMT_YUV420P, dst_width,dst_height);
 
	if (ret < 0) {
		pthread_mutex_unlock(&video_lock);
		return -2;
	}

	av_init_packet(&video_packet);
	video_packet.data = video_out_buffer;
	video_packet.size = video_out_buffer_size;
	
	ret = avcodec_encode_video2(video_context, &video_packet, video_frame, &got_video_packet);
	if (ret < 0) {

		pthread_mutex_unlock(&video_lock);
		return -11;
	}
	
	video_frame->pts = video_frame->pts + 1;

	if (got_video_packet == 0) {
		pthread_mutex_unlock(&video_lock);
		return -12;
	}
	
	if (video_packet.pts != AV_NOPTS_VALUE) {
		video_packet.pts = ( TSCalculate() - initialTSvalue);
	}

	if (video_packet.dts != AV_NOPTS_VALUE) {
		video_packet.dts = video_packet.pts;
	}

	video_packet.flags = video_packet.flags | AV_PKT_FLAG_KEY;
	video_packet.stream_index = video_stream->index;
	video_packet.duration = 1000/10;
	
	
	pthread_mutex_lock(&write_lock);		
	ret = av_interleaved_write_frame(av_context, &video_packet);
	pthread_mutex_unlock(&write_lock);

	
	av_free_packet(&video_packet);

    free(yuv_i420);

	pthread_mutex_unlock(&video_lock);

	return 0;
}

int FreyaStreamCore::AudioPusher(unsigned char * data, unsigned int len, long ts)
{
	
		

	int ret              = 0;
	int got_audio_packet = 0;
	
	if (is_audio_ready_flag == 0 || is_session_start_flag==0) {
		av_free_packet(&audio_packet);
        audio_codec = NULL;
        audio_stream = NULL;
		return -1;
	}
	
	pthread_mutex_lock(&audio_lock);
	
	int rettt = avcodec_fill_audio_frame(audio_frame, audio_context->channels,audio_context->sample_fmt, (const uint8_t*) data, audio_data_size_value, 0);

	audio_packet.data = NULL;
    audio_packet.size = 0;
	
	ret = avcodec_encode_audio2(audio_context, &audio_packet, audio_frame,&got_audio_packet);
		
		
	if (ret < 0) {
		pthread_mutex_unlock(&audio_lock);
		return -2;
	}

	
	audio_frame->pts = audio_frame->pts + audio_frame->nb_samples;

	if (got_audio_packet < 1) {
		pthread_mutex_unlock(&audio_lock);
		return -11;
	}

	if (audio_packet.pts != AV_NOPTS_VALUE) {
		audio_packet.pts = ( TSCalculate() - initialTSvalue);
	}
	if (audio_packet.dts != AV_NOPTS_VALUE) {
		audio_packet.dts =audio_packet.pts;
	}

	audio_packet.flags = audio_packet.flags | AV_PKT_FLAG_KEY;
	audio_packet.stream_index = audio_stream->index;
	audio_packet.duration = 1024 * 1000/ sample_rate_value;
	
	
	pthread_mutex_lock(&write_lock);
	ret = av_interleaved_write_frame(av_context, &audio_packet);
	pthread_mutex_unlock(&write_lock);

	av_free_packet(&audio_packet);

	pthread_mutex_unlock(&audio_lock);

	return 0;
}
