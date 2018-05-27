//
// Created by carter on 2016/8/27.
//

//for dm2016 operate
#include   <stdio.h>
#include   <stdlib.h>
#include   <unistd.h>
#include   <sys/types.h>
#include   <sys/stat.h>
#include   <fcntl.h>
#include   <termios.h>
#include   <errno.h>
#include   <string.h>
#include   <jni.h>
#include   <pthread.h>
#include  <android/log.h>
#include   <sys/file.h>
#include <assert.h>
#include <linux/i2c.h>
#include "dm2016.h"

#define U8 unsigned char

#define LOG    "i2cdm"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__)

#define I2C_SLAVE 0x0703

typedef struct{
        char flag[3];
	    U8 out_type;
	    U8 out_mode;
        short lcd_w;
        short lcd_h;
        short lcd_xres;
        short lcd_yres;
        short lcd_ht;
        short lcd_vt;
        short lcd_hbp;
        short lcd_vbp;
        U8 lcd_hspw;
        U8 lcd_vspw;
        U8 lcd_dclk;
        U8 lcd_lvds_ch;
        U8 lcd_lvds_mode;
        U8 lcd_lvds_bitwidth;
        U8 lcd_swap_port;
        U8 boot_bl_off;
}panel_para;
long panel_para_checksum =0;

bool is_char(char** src){
    if((**src)>='a'&&(**src)<='z')
        return true;
    else
        return false;
}

int doc_num(char** src)
{
    int ret =0;
    while(**src)
    {
        if(((**src)>='0')&&((**src)<='9'))
        {
            ret = ret*10+(U8)(**src -'0');
            *src+=1;
            if(**src ==';'||**src =='<')
                break;
        }
        else if(**src ==';'||**src ==0x0d||**src ==0x0a)
            break;
        else
            *src+=1;
    }
    return ret;
}

char doc_goto(char** src,char* endstr) {
    U8 ret =0;
    while(**src)
    {
        //LOGI("src %s",*src);
        if(strncmp(*src,endstr,strlen(endstr))==0)
        {
            *src +=strlen(endstr);
            ret=1;
            break;
        }
        else if(strncmp(*src,"<",1)==0 ||strncmp(*src,"[",1)==0)
        {
            ret=2;
            break;
        }
        else
            *src+=1;
    }
    return ret;
}

panel_para panelcfg_parse(char* buff){
    panel_para para;
    char *p =buff;
    U8 HBi_lcd0_config=1;

    if(doc_goto(&p,"HBi_lcd0_config")==1)
        HBi_lcd0_config = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"screen0_output_type")==1)
        para.out_type = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"screen0_output_mode")==1)
        para.out_mode = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_x")==1)
        para.lcd_w = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_y")==1)
        para.lcd_h = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_xres")==1)
        para.lcd_xres = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_yres")==1)
        para.lcd_yres = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_dclk_freq")==1)
        para.lcd_dclk = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_hbp")==1)
        para.lcd_hbp = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_hv_hspw")==1)
        para.lcd_hspw = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_ht")==1)
        para.lcd_ht = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_vbp")==1)
        para.lcd_vbp = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_hv_vspw")==1)
        para.lcd_vspw = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_vt")==1)
        para.lcd_vt = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_lvds_ch")==1)
        para.lcd_lvds_ch = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_lvds_mode")==1)
        para.lcd_lvds_mode = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_lvds_bitwidth")==1)
        para.lcd_lvds_bitwidth = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"lcd_swap_port")==1)
        para.lcd_swap_port = doc_num(&p);
    p =buff;
    if(doc_goto(&p,"boot_bl_off")==1)
        para.boot_bl_off = doc_num(&p);
    if(HBi_lcd0_config)
        para.flag[0]='P';
    else
        para.flag[0]='0';
    para.flag[1]='N';
    para.flag[2]='L';

    //LOGI("para %d",para.lcd_w);
    //LOGI("para %d",para.lcd_h);
    //LOGI("para %d",para.lcd_dclk);
    //LOGI("para %d",para.lcd_vbp);
    //LOGI("para %d",para.lcd_vspw);
    //LOGI("para %d",para.lcd_lvds_ch);
    return para;
}

struct i2c_rdwr_ioctl_data {
/* WARNING: DO NOT EDIT, AUTO-GENERATED CODE - SEE TOP FOR INSTRUCTIONS */
 struct i2c_msg __user *msgs;
 __u32 nmsgs;
};

//#define PAGE_SIZE  8  //24c02
JNIEXPORT jint JNICALL native_save_panelcfg(JNIEnv * env, jobject thiz,jstring filepath,jstring devpath)
{
 LOGI("native_save_panelcfg");
    char *bufByte;
    FILE * stream;
    jint fd;
    int size = 0,res=0;
    jboolean iscopy;
    const char *dev = env->GetStringUTFChars(devpath, &iscopy);
    const char *path_utf = env->GetStringUTFChars(filepath, &iscopy);

    //LOGI("open file %s",path_utf);
    stream=fopen(path_utf, "rw");//O_RDWR);

    if (stream == NULL) {
        LOGE("Cannot open file %s",path_utf);
        return -1;
    }
    bufByte = (char*) malloc(2000);

    size=fread(bufByte,sizeof(char),2000,stream);
    fclose(stream);

    //LOGI("para %s",bufByte);
    panel_para para = panelcfg_parse(bufByte);

    //LOGI("para %d",para.lcd_w);
    //LOGI("para %d",para.lcd_h);
    //LOGI("para %d",para.lcd_dclk);
    //LOGI("para %d",para.lcd_xres);
    //LOGI("para %d",para.lcd_yres);
    //LOGI("para %d",para.lcd_ht);
    U8 *buf = (U8 *)&para;
    int len = sizeof(panel_para)/sizeof(U8);
    U8 i;

    fd =open(dev, O_RDWR);//O_RDWR);
    if (fd == -1) {
        LOGE("Cannot open dev %s",dev);
        return -1;
    }

    U8 adr = 0xa0>>1;
    //res = ioctl(fd,I2C_TENBIT,0);
    res = ioctl(fd, I2C_SLAVE, 0xa0>>1);

    LOGI("len %d",len);
    for(i=0;i<len;i++){
        //LOGI("write %x",buf[i]);
        panel_para_checksum +=buf[i];
    }

    for(i=0;i<len;i++){
        //LOGI("write %x",buf[i]);
        bufByte[0] = i;
        bufByte[1] = buf[i];
        res = write(fd, bufByte, 2);
        usleep(10000);
    }

    LOGI("write result %d",res);
    close(fd);
    return res;
}


unsigned long get_file_size(const char *path)
{
    unsigned long filesize = -1;
    struct stat statbuff;
    if(stat(path, &statbuff) < 0){
        return filesize;
    }else{
        filesize = statbuff.st_size;
    }
    return filesize;
}

//#define PAGE_SIZE  8  //24c02
JNIEXPORT jint JNICALL native_save_panelcfg_eeprom(JNIEnv * env, jobject thiz,jstring filepath,jstring devpath)
{
LOGI("native_save_panelcfg_eeprom");
    char *bufByte;
    FILE * stream;
    FILE *fd;
    int size = 0,res=0;
    jboolean iscopy;
    const char *dev = env->GetStringUTFChars(devpath, &iscopy);
    const char *path_utf = env->GetStringUTFChars(filepath, &iscopy);

    //LOGI("open file %s",path_utf);
    stream=fopen(path_utf, "rw");//O_RDWR);

    if (stream == NULL) {
        LOGE("Cannot open file %s",path_utf);
        return -1;
    }

    long fisize = get_file_size(path_utf);
    bufByte = (char*) malloc(fisize);
    size=fread(bufByte,sizeof(char),fisize,stream);
    fclose(stream);

    fd =fopen(dev, "rw");//O_RDWR);
    if (fd == NULL) {
        LOGE("Cannot open dev %s",dev);
        return -1;
    }

    size = fwrite(bufByte,sizeof(char),size,fd);
    LOGI("write result %d",size);
    fclose(fd);
    return size;
}


JNIEXPORT jint JNICALL native_save_macaddr(JNIEnv * env, jobject thiz,jbyteArray mac,jstring devpath)
{
LOGI("native_save_macaddr");
    unsigned char bufByte[10];
    FILE * stream;
    jint fd;
    int size = 0,res=0;
    int i=0;
    jboolean iscopy;
    const char *dev = env->GetStringUTFChars(devpath, &iscopy);
    jbyte* bBuffer = env->GetByteArrayElements(mac,0);

    fd =open(dev, O_RDWR);//O_RDWR);
    if (fd == -1) {
        LOGE("Cannot open dev %s",dev);
        return -1;
    }
    U8 adr = 0xa0>>1;
    //res = ioctl(fd,I2C_TENBIT,0);
    res = ioctl(fd, I2C_SLAVE, 0xa0>>1);

    for(i=0;i<6;i++){
        bufByte[0] = i+0x70;
        bufByte[1] = bBuffer[i];
        //LOGI("write %x",bBuffer[i]);
        res = write(fd, bufByte, 2);
        usleep(10000);
    }

    return 1;
}

JNIEXPORT jint JNICALL native_checklicense(JNIEnv * env, jobject obj,jstring devpath)
{
    unsigned char *bufByte;
    int res = 0, i = 0, j = 0;
    // encrypt keys
    int i2c_addr = 0xa0;
    unsigned char Key[16] = { 0xff,0xee,0xdd,0xcc,0x66,0x54,0x32,0x10,0xff,0xee,0xdd,0xcc,0x66,0x54,0x32,0x10};
    unsigned char srcData[8];
    unsigned char decData[8];
    jboolean iscopy;
    const char *dev = env->GetStringUTFChars(devpath, &iscopy);
    jint fileHander =open(dev, O_RDWR);
    /////0xa0
    for(i=0;i<8;i++) {
        srcData[i]=decData[i] =rand()%255;
        LOGI("srcData slave %d %d",srcData[i],decData[i]);
    }
//    res = EDesEn_Crypt(decData,Key); //true  error
    LOGI("EDesEn_Crypt slave %d",res);
    //for(i=0;i<8;i++)
    {
        //LOGI("Crypt %d",decData[i]);
    }

    res=ioctl(fileHander, I2C_SLAVE, i2c_addr>>1);
    LOGI("ioctl slave %d",res);
    bufByte = (unsigned char*) malloc(10);
    bufByte[0] = 0x90;

    for(i=0;i<8;i++) {
        bufByte[i+1]=decData[i]; LOGI("Crypt %d",decData[i]);
    }

    res = write(fileHander, bufByte, 9);
    LOGI("write res1 %d",res);
    res = write(fileHander, bufByte, 1);
    LOGI("write res2 %d",res);
    res = read(fileHander, bufByte, 8);
    LOGI("read res2 %d",res);

    res =1;
    for(i=0;i<8;i++) {
        LOGI("srcData decData %d %d",srcData[i],bufByte[i]);
        if(srcData[i]!=bufByte[i]) {
            res =0;
            break;
        }
    }
 LOGI("read res2 %d",res);
    close(fileHander);
    return res;
}

static JNINativeMethod gMethods[] = {
        { "save_panelcfg", "(Ljava/lang/String;Ljava/lang/String;)I",(void*) native_save_panelcfg },
        { "save_panelcfg_eeprom", "(Ljava/lang/String;Ljava/lang/String;)I",(void*) native_save_panelcfg_eeprom },
        { "save_macaddr", "([BLjava/lang/String;)I",(void*) native_save_macaddr },
        { "checklicense", "(Ljava/lang/String;)I",(void*) native_checklicense },
};

/*
 * 为某一个类注册本地方法
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
        JNINativeMethod* gMethods, int numMethods) {
        LOGI("registerNativeMethods");
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * 为所有类注册本地方法
 */
static int registerNatives(JNIEnv* env) {
    const char* kClassName = "bid/vcoo/plat/I2Cdm"; //指定要注册的类
    LOGI("registerNatives");
    return registerNativeMethods(env, kClassName, gMethods,
            sizeof(gMethods) / sizeof(gMethods[0]));
}

/*
 * System.loadLibrary("lib")时调用
 * 如果成功返回JNI版本, 失败返回-1
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;
LOGI("JNI_OnLoad");
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    assert(env != NULL);

    if (!registerNatives(env)) { //注册
        return -1;
    }
    //成功
    result = JNI_VERSION_1_4;

    return result;
}