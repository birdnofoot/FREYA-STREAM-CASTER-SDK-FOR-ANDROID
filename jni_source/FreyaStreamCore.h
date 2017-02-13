#include <pthread.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/time.h>


extern "C"
{
#ifdef __cplusplus
  #define  __STDC_LIMIT_MACROS
  #define  __STDC_CONSTANT_MACROS
  #define __STDC_FORMAT_MACROS
    #ifdef _STDINT_H
      #undef _STDINT_H
    #endif
  #include <stdint.h>
#endif
}

#ifdef __cplusplus
extern "C"
{
#endif
#include "libavutil/opt.h"
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libavutil/avutil.h"
#include "include/libswscale/swscale.h"
#include "version.h"
#ifdef __cplusplus
}
#endif


class FreyaStreamCore
{
public:
	FreyaStreamCore();
	~FreyaStreamCore();

	void Initialize();
	void Destroy();

	int setupRtmpUrl(const char* url);
	int setupVideoParameter(int width, int height, int bitrate, int fps, const char* h264profile, const char* h264encodemode);
	int setupAudioParameter(int sample_rate, int channels, int soundrate, const char* aacprofile);

	int startSession();
	int stopSession();

	int VideoPusher(unsigned char * data, unsigned int size, int in_width, int in_height, long ts, int is_front,int orientation);
	int AudioPusher(unsigned char * data, unsigned int size, long ts);

protected:
private:
	
	void CloseVideoCode();
	void CloseAudioCode();

	AVPacket video_packet;
	AVPacket audio_packet;

	AVFormatContext *av_context;

	AVStream *audio_stream;
	AVStream *video_stream;

	double audio_pts_value;
	double video_pts_value;

	AVCodec *video_codec;
	AVCodec *audio_codec;

	AVCodecContext *video_context;
	AVCodecContext *audio_context;

	uint8_t *video_out_buffer;
	uint8_t *audio_out_buffer;

	uint8_t *audio_frame_buffer;

	int video_out_buffer_size;
	int audio_out_buffer_size;
	int audio_input_frame_size;

	struct AVFrame * video_frame;
	struct AVFrame * audio_frame;

	int video_bitrate_value;
	int video_fps_value;

	int audio_data_size_value;

	int is_video_ready_flag;
	int is_audio_ready_flag;
	int is_session_start_flag;

	pthread_mutex_t video_lock;
	pthread_mutex_t audio_lock;
	pthread_mutex_t write_lock;

	long long  initialTSvalue;

	
};