LOCAL_PATH :=$(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := FreyaStreamSDK

LOCAL_SRC_FILES := \
		com_freya_stream_caster_sdk_FreyaStreamSDK.cpp \
		FreyaStreamCore.cpp \

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH) \
	$(LOCAL_PATH)/include/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI) \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavutil/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavcodec/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavformat/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavfilter/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libavresample \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libswscale/ \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libpostproc \
    $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/polarssl \
	$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include/libswresample/  
	
LOCAL_DISABLE_FATAL_LINKER_WARNINGS=true
	
ifeq ($(TARGET_ARCH_ABI),armeabi)
    LOCAL_CFLAGS += -I$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
    LOCAL_CFLAGS += -marm -mfpu=vfp -mfloat-abi=softfp -fPIC -O3 -march=armv5te -mtune=arm1176jzf-s -ftree-vectorize -ffast-math
    LOCAL_ARM_MODE := arm
endif
	
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_CFLAGS += -I$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
    LOCAL_CFLAGS += -marm -fPIC -O3 -march=armv7-a -mfpu=neon -mtune=generic-armv7-a -mfloat-abi=softfp -ftree-vectorize -mvectorize-with-neon-quad -ffast-math
    LOCAL_ARM_NEON := true
    LOCAL_ARM_MODE := arm
endif
	
ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_CFLAGS += -I$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
    LOCAL_CFLAGS += -fPIC -O3 -march=armv8-a -mtune=cortex-a57.cortex-a53 -ftree-vectorize -ffast-math
    LOCAL_ARM_NEON := true
    LOCAL_ARM_MODE := arm
endif

ifeq ($(TARGET_ARCH_ABI),x86)
    LOCAL_CFLAGS += -I$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
    LOCAL_CFLAGS += -fPIC -O3 -march=atom -mtune=atom -mssse3 -ffast-math -ftree-vectorize -mfpmath=sse
endif

ifeq ($(TARGET_ARCH_ABI),x86_64)
    LOCAL_CFLAGS += -I$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
    LOCAL_CFLAGS += -fPIC -O3 -march=atom -mtune=atom -mssse3 -ffast-math -ftree-vectorize -mfpmath=sse
endif

ifeq ($(TARGET_ARCH_ABI),mips)
    LOCAL_CFLAGS += -I$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
    LOCAL_CFLAGS += -fPIC -O3 -march=mips32r2 -mtune=mips32r2 -mabi=32 -mhard-float -mfp64 -mmsa -mdspr2 -mmt -mmcu -ffast-math -ftree-vectorize
endif

ifeq ($(TARGET_ARCH_ABI),mips64)
    LOCAL_CFLAGS += -I$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/include
    LOCAL_CFLAGS += -fPIC -O3 -march=mips64r6 -mtune=mips64r6 -mabi=64 -mhard-float -mfp64 -mmsa -mmt -mdspr2 -mmcu -ffast-math -ftree-vectorize
endif

LOCAL_LDLIBS := -llog -lGLESv2 -lz
LOCAL_LDLIBS += -L$(LOCAL_PATH)/$(TARGET_ARCH_ABI)/lib -lavformat -lavcodec -lpostproc -lavfilter -lavresample -lswscale -lswresample -lavutil -lfdk-aac -lx264 -lrtmp -lpolarssl -lyuv 

include $(BUILD_SHARED_LIBRARY)
