package com.taisau.substation.util;

/**
 * Created by Administrator on 2016/11/15 0015.
 */

public class DetectInfo {
    public DetectInfo(){
        pos=new FacePos();
        frame=new FaceFrame();
    }
    private byte []bb=new byte[144];
    private FacePos pos;
    private FaceFrame frame;
    public byte[] getBb() {
        return bb;
    }

    public void setBb(byte[] bb) {
        this.bb = bb;
    }

    public FacePos getPos() {
        return pos;
    }

    public FaceFrame getFrame() {
        return frame;
    }

    /*public  void initFaceFrame(){

        frame.setLeft(byte2long(bb[0],bb[1],bb[2],bb[3]));
        frame.setTop(byte2long(bb[8],bb[9],bb[10],bb[11]));
        frame.setRight(byte2long(bb[16],bb[17],bb[18],bb[19]));
        frame.setBottom(byte2long(bb[24],bb[25],bb[26],bb[27]));
    }
    public void initFacePos(){
        pos.setEye_left_x(byte2long(bb[64],bb[65],bb[50],bb[51]));
        pos.setEye_left_y(byte2long(bb[72],bb[73],bb[74],bb[75]));
        pos.setEye_right_x(byte2long(bb[80],bb[81],bb[82],bb[83]));
        pos.setEye_right_y(byte2long(bb[88],bb[89],bb[90],bb[91]));
        pos.setNose_x(byte2long(bb[96],bb[97],bb[98],bb[99]));
        pos.setNose_y(byte2long(bb[104],bb[105],bb[106],bb[107]));
        pos.setMouth_left_x(byte2long(bb[112],bb[113],bb[114],bb[115]));
        pos.setMouth_left_y(byte2long(bb[120],bb[121],bb[122],bb[123]));
        pos.setMouth_right_x(byte2long(bb[128],bb[129],bb[130],bb[131]));
        pos.setMouth_right_y(byte2long(bb[136],bb[137],bb[138],bb[139]));
    }*/

    public  void initFaceFrame(){
        frame.setLeft(byte2long(bb[0],bb[1],bb[2],bb[3]));
        frame.setTop(byte2long(bb[4],bb[5],bb[6],bb[7]));
        frame.setRight(byte2long(bb[8],bb[9],bb[10],bb[11]));
        frame.setBottom(byte2long(bb[12],bb[13],bb[14],bb[15]));
    }
    public void initFacePos(){
        pos.setEye_left_x(byte2long(bb[48],bb[49],bb[50],bb[51]));
        pos.setEye_left_y(byte2long(bb[52],bb[53],bb[54],bb[55]));
        pos.setEye_right_x(byte2long(bb[56],bb[57],bb[58],bb[59]));
        pos.setEye_right_y(byte2long(bb[60],bb[61],bb[62],bb[63]));
        pos.setNose_x(byte2long(bb[64],bb[65],bb[66],bb[67]));
        pos.setNose_y(byte2long(bb[68],bb[69],bb[70],bb[71]));
        pos.setMouth_left_x(byte2long(bb[72],bb[73],bb[74],bb[75]));
        pos.setMouth_left_y(byte2long(bb[76],bb[77],bb[78],bb[79]));
        pos.setMouth_right_x(byte2long(bb[80],bb[81],bb[82],bb[83]));
        pos.setMouth_right_y(byte2long(bb[84],bb[85],bb[86],bb[87]));
    }

    public long byte2long(byte a,byte b,byte c,byte d){
      //  System.out.println("before a:"+a+" b:"+b+" c:"+c+" d:"+d);
        long j,k,l,m;
        if(a<0&b==-1&c==-1&d==-1) {
            return (long) a;
        }
        else {
            j = (long) (a & 0xff);
            k = (long) (b & 0xff) << 8;
            l = (long) (c & 0xff) << 16;
            m = (long) (d & 0xff) << 24;
          //  System.out.println("after a:" + j + " b:" + k + " c:" + l + " d:" + m);
            return j + k + l + m;
        }
    }
    public class FaceFrame{
        private long left;
        private long right;
        private long bottom;
        private long top;

        public FaceFrame(long left, long right, long bottom, long top) {
            this.left = left;
            this.right = right;
            this.bottom = bottom;
            this.top = top;
        }

        public FaceFrame() {

        }

        public long getLeft() {
            return left;
        }

        public void setLeft(long left) {
            this.left = left;
        }

        public long getRight() {
            return right;
        }

        public void setRight(long right) {
            this.right = right;
        }

        public long getBottom() {
            return bottom;
        }

        public void setBottom(long bottom) {
            this.bottom = bottom;
        }

        public long getTop() {
            return top;
        }

        public void setTop(long top) {
            this.top = top;
        }
    }
    public static class FacePos{
        private long eye_left_x;
        private long eye_left_y;
        private long eye_right_x;
        private long eye_right_y;
        private long nose_x;
        private long nose_y;
        private long mouth_left_x;
        private long mouth_left_y;
        private long mouth_right_x;
        private long mouth_right_y;

        public FacePos() {
        }

        public FacePos(long eye_left_x, long eye_left_y, long eye_right_x, long eye_right_y,
                       long nose_x, long nose_y, long mouth_left_x, long mouth_left_y,
                       long mouth_right_x, long mouth_right_y) {
            this.eye_left_x = eye_left_x;
            this.eye_left_y = eye_left_y;
            this.eye_right_x = eye_right_x;
            this.eye_right_y = eye_right_y;
            this.nose_x = nose_x;
            this.nose_y = nose_y;
            this.mouth_left_x = mouth_left_x;
            this.mouth_left_y = mouth_left_y;
            this.mouth_right_x = mouth_right_x;
            this.mouth_right_y = mouth_right_y;
        }

        public long getEye_left_x() {
            return eye_left_x;
        }

        public void setEye_left_x(long eye_left_x) {
            this.eye_left_x = eye_left_x;
        }

        public long getEye_left_y() {
            return eye_left_y;
        }

        public void setEye_left_y(long eye_left_y) {
            this.eye_left_y = eye_left_y;
        }

        public long getEye_right_x() {
            return eye_right_x;
        }

        public void setEye_right_x(long eye_right_x) {
            this.eye_right_x = eye_right_x;
        }

        public long getEye_right_y() {
            return eye_right_y;
        }

        public void setEye_right_y(long eye_right_y) {
            this.eye_right_y = eye_right_y;
        }

        public long getNose_x() {
            return nose_x;
        }

        public void setNose_x(long nose_x) {
            this.nose_x = nose_x;
        }

        public long getNose_y() {
            return nose_y;
        }

        public void setNose_y(long nose_y) {
            this.nose_y = nose_y;
        }

        public long getMouth_left_x() {
            return mouth_left_x;
        }

        public void setMouth_left_x(long mouth_left_x) {
            this.mouth_left_x = mouth_left_x;
        }

        public long getMouth_left_y() {
            return mouth_left_y;
        }

        public void setMouth_left_y(long mouth_left_y) {
            this.mouth_left_y = mouth_left_y;
        }

        public long getMouth_right_x() {
            return mouth_right_x;
        }

        public void setMouth_right_x(long mouth_right_x) {
            this.mouth_right_x = mouth_right_x;
        }

        public long getMouth_right_y() {
            return mouth_right_y;
        }

        public void setMouth_right_y(long mouth_right_y) {
            this.mouth_right_y = mouth_right_y;
        }
    }
}
