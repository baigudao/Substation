
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

TARGET_PLATFORM := android-22
LOCAL_MODULE    := serial_port
LOCAL_SRC_FILES := SerialPort.c
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
#
#模块2
#
#清除变量
include $(CLEAR_VARS)
#生成模块2的名称
LOCAL_MODULE    := I2Cdm
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS := \
	-ldl \
	-llog \

LOCAL_SRC_FILES := \
	I2Cdm.cpp \
  	#dm2016.o \
	empty.c \


#编译生成共享库
include $(BUILD_SHARED_LIBRARY)

#产生两个.so动态库文件即serial_port.so和 I2Cdm.so