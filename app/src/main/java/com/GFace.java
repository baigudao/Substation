package com;

/**
 * Created by Administrator on 2017/6/21 0021.
 */

public class GFace {

    static {
        System.loadLibrary("GFaceNew");
        System.loadLibrary("caffe");
    }

    public static native int loadModel(String model_D, String model_A, String model_DB, String model_P);//加载模型文件，返回0说明模型加载成功；
    public static native byte[] detectFace(byte[]buf,int width,int height);//检测人脸返回byte[]，返回值不为null并且byte[0]>0说明检测到人脸；//data[]适当压缩剪裁可提高检测效率
    public static native float[] getFea(byte[]buf,int width,int height,int eye_lx,int eye_ly,int eye_rx,
                                       int eye_ry,int nose_x,int nose_y,int mouse_lx,int mouse_ly,int mouse_rx,int mouse_ry);//提取人脸图像特征值 : 长度512的float array；//持久化保存就是人脸模版了
    public static native float feaCompare(float[]fea1,float[]fea2);//比对两个特征值，返回相似分值；
    public static native String GetSn(String key);
    public static native int SetKey(String key);
    static  public FaceInfo info = new FaceInfo();
    //坐标点
    public static class POINT {
        public  long x=0;
        public  long y=0;
    }
    //人脸坐标
    public  static class RECT {
        public  long  left=0;
        public  long  top=0;
        public  long  right=0;
        public  long  bottom=0;
    }
    //关键点坐标（眼睛 鼻子 嘴巴）
    public  static class FacePointInfo {
        public POINT ptEyeLeft;
        public POINT ptEyeRight;
        public POINT ptNose;
        public POINT ptMouthLeft;
        public POINT ptMouthRight;
    }
    //人脸信息
    public static class  FaceInfo {
        public  long count;
        public  RECT []rc;//=new RECT[10];//系统最多返回10个人脸
        public FacePointInfo[] info;// = new FacePointInfo[10];//系统最多返回10个人脸

        public FaceInfo() {
            this.rc = new RECT[10];//系统最多返回10个人脸 底层分配10个人脸
            this.info =  new FacePointInfo[10];//系统最多返回10个人脸 底层分配10个人脸
        }
    }

    private static  long byte2long(byte [] a,int nIndex,boolean bX64){
        //  System.out.println("before a:"+a+" b:"+b+" c:"+c+" d:"+d);
        if(bX64)
        {

            long j,k,l,m,j1,k1,l1,m1;

            j = (long) (a[0 + nIndex] & 0xff);
            k = (long) (a[1 + nIndex] & 0xff) << 8;
            l = (long) (a[2 + nIndex] & 0xff) << 16;
            m = (long) (a[3 + nIndex] & 0xff) << 24;

            j1 = (long) (a[4 + nIndex] & 0xff)<< 32;
            k1 = (long) (a[5 + nIndex] & 0xff) << 40;
            l1 = (long) (a[6 + nIndex] & 0xff) << 48;
            m1 = (long) (a[7 + nIndex] & 0xff) << 56;
            //  System.out.println("after a:" + j + " b:" + k + " c:" + l + " d:" + m);
            return j + k + l + m +j1+k1+l1+m1;

        }
        else
        {

            long j,k,l,m;

            j = (long) (a[0 + nIndex] & 0xff);
            k = (long) (a[1 + nIndex] & 0xff) << 8;
            l = (long) (a[2 + nIndex] & 0xff) << 16;
            m = (long) (a[3 + nIndex] & 0xff) << 24;
            //  System.out.println("after a:" + j + " b:" + k + " c:" + l + " d:" + m);
            return j + k + l + m;

        }

    }
    //解析人脸信息
    public static  FaceInfo getFaceInfo(byte[] buf){
        int nSize  = 8;
        int nIndex =0;
        boolean bX64 = true;
        if(buf.length < 1128)
        {
            nSize = 4;
            bX64 = false;

        }
        long nCount = byte2long(buf,nIndex,bX64);
        nIndex += nSize;

        info.count = nCount;
        for (int i = 0; i< 10;i++)
        {

            if (i >= nCount)
            {
                nIndex += (10 - nCount)*nSize*4;
                break;
            }
            info.rc[i]=new RECT();
            info.rc[i].left =  byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.rc[i].top =  byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.rc[i].right =  byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.rc[i].bottom =  byte2long(buf,nIndex,bX64);
            nIndex += nSize;

        }
        for (int i = 0; i< 10;i++)
        {
            if (i >= nCount)
            {
                nIndex += (10 - nCount)*nSize*10;
                break;
            }
            info.info[i]=new FacePointInfo();
            info.info[i].ptEyeLeft = new POINT();
            info.info[i].ptEyeLeft.x = byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.info[i].ptEyeLeft.y = byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.info[i].ptEyeRight = new POINT();
            info.info[i].ptEyeRight.x = byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.info[i].ptEyeRight.y = byte2long(buf,nIndex,bX64);
            nIndex += nSize;

            info.info[i].ptNose = new POINT();
            info.info[i].ptNose.x = byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.info[i].ptNose.y = byte2long(buf,nIndex,bX64);
            nIndex += nSize;

            info.info[i].ptMouthLeft = new POINT();
            info.info[i].ptMouthLeft.x = byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.info[i].ptMouthLeft.y = byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.info[i].ptMouthRight = new POINT();
            info.info[i].ptMouthRight.x = byte2long(buf,nIndex,bX64);
            nIndex += nSize;
            info.info[i].ptMouthRight.y = byte2long(buf,nIndex,bX64);
            nIndex += nSize;

        }

        return  info;
    }

}
