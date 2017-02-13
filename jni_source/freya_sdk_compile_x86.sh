#!/bin/bash
CURRENT_PATH=/home/freya/jni
#本脚本所在的目录位置
NDKROOT_PATH=/home/freya/android-ndk-r13
#NDK所在目录
PLATFORM_X86_API=android-15
#对应X86的ANDROID API
PLATFORM_NDK_TOOLCHAIN_VERSION=4.9
#对应的TOOLCHAIN版本

##########################################################配置项到此结束##########################################################

#预处理开始
trap "" INT
mkdir -p $CURRENT_PATH/freya_sdk_build_finished/x86
rm -rf $CURRENT_PATH/freya_sdk_build_finished/x86/*.so
rm -rf $CURRENT_PATH/Application.mk
export PATH=$PATH:$NDKROOT_PATH
#预处理结束

#编译X86开始
echo "APP_PLATFORM := $PLATFORM_X86_API" >> $CURRENT_PATH/Application.mk
echo "APP_OPTIM := release" >> $CURRENT_PATH/Application.mk
echo "APP_ABI := x86" >> $CURRENT_PATH/Application.mk
echo "APP_STL := gnustl_static" >> $CURRENT_PATH/Application.mk
echo "APP_CPPFLAGS := -frtti -fexceptions" >> $CURRENT_PATH/Application.mk
echo "NDK_TOOLCHAIN_VERSION := $PLATFORM_NDK_TOOLCHAIN_VERSION" >> $CURRENT_PATH/Application.mk
ndk-build APP_BUILD_SCRIPT=$CURRENT_PATH/Android.mk NDK_APPLICATION_MK=$CURRENT_PATH/Application.mk NDK_PROJECT_PATH=$CURRENT_PATH/
rm -rf $CURRENT_PATH/Application.mk
cp -R $CURRENT_PATH/libs/x86/*.so $CURRENT_PATH/freya_sdk_build_finished/x86
#编译X86结束

#后处理开始
cd $CURRENT_PATH
chmod -R 777 *
#后处理结束