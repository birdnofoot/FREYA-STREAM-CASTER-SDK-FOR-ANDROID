#!/bin/bash
CURRENT_PATH=/home/freya/jni
#本脚本所在的目录位置
NDKROOT_PATH=/home/freya/android-ndk-r13
#NDK所在目录
PLATFORM_ARM_API=android-15
#对应ARM的ANDROID API
PLATFORM_ARMV7A_API=android-15
#对应ARMV7-A的ANDROID API
PLATFORM_ARM64V8A_API=android-21
#对应ARM64-V8A的ANDROID API
PLATFORM_X86_API=android-15
#对应X86的ANDROID API
PLATFORM_X86_64_API=android-21
#对应X86_64的ANDROID API
PLATFORM_MIPS_API=android-15
#对应MIPS的ANDROID API
PLATFORM_MIPS64_API=android-21
#对应MIPS64的ANDROID API
PLATFORM_NDK_TOOLCHAIN_VERSION=4.9
#对应的TOOLCHAIN版本

##########################################################配置项到此结束##########################################################

#预处理开始
trap "" INT
mkdir -p $CURRENT_PATH/freya_sdk_build_finished/armeabi
rm -rf $CURRENT_PATH/freya_sdk_build_finished/armeabi/*.so
mkdir -p $CURRENT_PATH/freya_sdk_build_finished/armeabi-v7a
rm -rf $CURRENT_PATH/freya_sdk_build_finished/armeabi-v7a/*.so
mkdir -p $CURRENT_PATH/freya_sdk_build_finished/arm64-v8a
rm -rf $CURRENT_PATH/freya_sdk_build_finished/arm64-v8a/*.so
mkdir -p $CURRENT_PATH/freya_sdk_build_finished/x86
rm -rf $CURRENT_PATH/freya_sdk_build_finished/x86/*.so
mkdir -p $CURRENT_PATH/freya_sdk_build_finished/x86_64
rm -rf $CURRENT_PATH/freya_sdk_build_finished/x86_64/*.so
mkdir -p $CURRENT_PATH/freya_sdk_build_finished/mips
rm -rf $CURRENT_PATH/freya_sdk_build_finished/mips/*.so
mkdir -p $CURRENT_PATH/freya_sdk_build_finished/mips64
rm -rf $CURRENT_PATH/freya_sdk_build_finished/mips64/*.so
rm -rf $CURRENT_PATH/Application.mk
export PATH=$PATH:$NDKROOT_PATH
#预处理结束

#编译ARM开始
echo "APP_PLATFORM := $PLATFORM_ARM_API" >> $CURRENT_PATH/Application.mk
echo "APP_OPTIM := release" >> $CURRENT_PATH/Application.mk
echo "APP_ABI := armeabi" >> $CURRENT_PATH/Application.mk
echo "APP_STL := gnustl_static" >> $CURRENT_PATH/Application.mk
echo "APP_CPPFLAGS := -frtti -fexceptions" >> $CURRENT_PATH/Application.mk
echo "NDK_TOOLCHAIN_VERSION := $PLATFORM_NDK_TOOLCHAIN_VERSION" >> $CURRENT_PATH/Application.mk
ndk-build APP_BUILD_SCRIPT=$CURRENT_PATH/Android.mk NDK_APPLICATION_MK=$CURRENT_PATH/Application.mk NDK_PROJECT_PATH=$CURRENT_PATH/
rm -rf $CURRENT_PATH/Application.mk
cp -R $CURRENT_PATH/libs/armeabi/*.so $CURRENT_PATH/freya_sdk_build_finished/armeabi
#编译ARM结束

#编译ARMV7-A开始
echo "APP_PLATFORM := $PLATFORM_ARMV7A_API" >> $CURRENT_PATH/Application.mk
echo "APP_OPTIM := release" >> $CURRENT_PATH/Application.mk
echo "APP_ABI := armeabi-v7a" >> $CURRENT_PATH/Application.mk
echo "APP_STL := gnustl_static" >> $CURRENT_PATH/Application.mk
echo "APP_CPPFLAGS := -frtti -fexceptions" >> $CURRENT_PATH/Application.mk
echo "NDK_TOOLCHAIN_VERSION := $PLATFORM_NDK_TOOLCHAIN_VERSION" >> $CURRENT_PATH/Application.mk
ndk-build APP_BUILD_SCRIPT=$CURRENT_PATH/Android.mk NDK_APPLICATION_MK=$CURRENT_PATH/Application.mk NDK_PROJECT_PATH=$CURRENT_PATH/
rm -rf $CURRENT_PATH/Application.mk
cp -R $CURRENT_PATH/libs/armeabi-v7a/*.so $CURRENT_PATH/freya_sdk_build_finished/armeabi-v7a
#编译ARMV7-A结束

#编译ARM64-V8A开始
echo "APP_PLATFORM := $PLATFORM_ARM64V8A_API" >> $CURRENT_PATH/Application.mk
echo "APP_OPTIM := release" >> $CURRENT_PATH/Application.mk
echo "APP_ABI := arm64-v8a" >> $CURRENT_PATH/Application.mk
echo "APP_STL := gnustl_static" >> $CURRENT_PATH/Application.mk
echo "APP_CPPFLAGS := -frtti -fexceptions" >> $CURRENT_PATH/Application.mk
echo "NDK_TOOLCHAIN_VERSION := $PLATFORM_NDK_TOOLCHAIN_VERSION" >> $CURRENT_PATH/Application.mk
ndk-build APP_BUILD_SCRIPT=$CURRENT_PATH/Android.mk NDK_APPLICATION_MK=$CURRENT_PATH/Application.mk NDK_PROJECT_PATH=$CURRENT_PATH/
rm -rf $CURRENT_PATH/Application.mk
cp -R $CURRENT_PATH/libs/arm64-v8a/*.so $CURRENT_PATH/freya_sdk_build_finished/arm64-v8a
#编译ARM64-V8A结束

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

#编译X86_64开始
echo "APP_PLATFORM := $PLATFORM_X86_64_API" >> $CURRENT_PATH/Application.mk
echo "APP_OPTIM := release" >> $CURRENT_PATH/Application.mk
echo "APP_ABI := x86_64" >> $CURRENT_PATH/Application.mk
echo "APP_STL := gnustl_static" >> $CURRENT_PATH/Application.mk
echo "APP_CPPFLAGS := -frtti -fexceptions" >> $CURRENT_PATH/Application.mk
echo "NDK_TOOLCHAIN_VERSION := $PLATFORM_NDK_TOOLCHAIN_VERSION" >> $CURRENT_PATH/Application.mk
ndk-build APP_BUILD_SCRIPT=$CURRENT_PATH/Android.mk NDK_APPLICATION_MK=$CURRENT_PATH/Application.mk NDK_PROJECT_PATH=$CURRENT_PATH/
rm -rf $CURRENT_PATH/Application.mk
cp -R $CURRENT_PATH/libs/x86_64/*.so $CURRENT_PATH/freya_sdk_build_finished/x86_64
#编译X86_64结束

#编译MIPS开始
echo "APP_PLATFORM := $PLATFORM_MIPS_API" >> $CURRENT_PATH/Application.mk
echo "APP_OPTIM := release" >> $CURRENT_PATH/Application.mk
echo "APP_ABI := mips" >> $CURRENT_PATH/Application.mk
echo "APP_STL := gnustl_static" >> $CURRENT_PATH/Application.mk
echo "APP_CPPFLAGS := -frtti -fexceptions" >> $CURRENT_PATH/Application.mk
echo "NDK_TOOLCHAIN_VERSION := $PLATFORM_NDK_TOOLCHAIN_VERSION" >> $CURRENT_PATH/Application.mk
ndk-build APP_BUILD_SCRIPT=$CURRENT_PATH/Android.mk NDK_APPLICATION_MK=$CURRENT_PATH/Application.mk NDK_PROJECT_PATH=$CURRENT_PATH/
rm -rf $CURRENT_PATH/Application.mk
cp -R $CURRENT_PATH/libs/mips/*.so $CURRENT_PATH/freya_sdk_build_finished/mips
#编译MIPS结束

#编译MIPS64开始
echo "APP_PLATFORM := $PLATFORM_MIPS64_API" >> $CURRENT_PATH/Application.mk
echo "APP_OPTIM := release" >> $CURRENT_PATH/Application.mk
echo "APP_ABI := mips64" >> $CURRENT_PATH/Application.mk
echo "APP_STL := gnustl_static" >> $CURRENT_PATH/Application.mk
echo "APP_CPPFLAGS := -frtti -fexceptions" >> $CURRENT_PATH/Application.mk
echo "NDK_TOOLCHAIN_VERSION := $PLATFORM_NDK_TOOLCHAIN_VERSION" >> $CURRENT_PATH/Application.mk
ndk-build APP_BUILD_SCRIPT=$CURRENT_PATH/Android.mk NDK_APPLICATION_MK=$CURRENT_PATH/Application.mk NDK_PROJECT_PATH=$CURRENT_PATH/
rm -rf $CURRENT_PATH/Application.mk
cp -R $CURRENT_PATH/libs/mips64/*.so $CURRENT_PATH/freya_sdk_build_finished/mips64
#编译MIPS64结束

#后处理开始
cd $CURRENT_PATH
chmod -R 777 *
#后处理结束