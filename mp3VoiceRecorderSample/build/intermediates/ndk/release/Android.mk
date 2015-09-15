LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := mp3lame
LOCAL_SRC_FILES := \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\Android.mk \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\com_uraroji_garage_android_lame_SimpleLame.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\Note.txt \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\bitstream.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\encoder.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\fft.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\gain_analysis.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\id3tag.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\lame.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\lame.rc \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\mpglib_interface.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\newmdct.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\presets.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\psymodel.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\quantize.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\quantize_pvt.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\reservoir.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\set_get.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\tables.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\takehiro.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\util.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\vbrquantize.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\VbrTag.c \
	E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni\lame-3.98.4_libmp3lame\version.c \

LOCAL_C_INCLUDES += E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\main\jni
LOCAL_C_INCLUDES += E:\PublicBuddy-AndroidApp\mp3VoiceRecorderSample\src\release\jni

include $(BUILD_SHARED_LIBRARY)
