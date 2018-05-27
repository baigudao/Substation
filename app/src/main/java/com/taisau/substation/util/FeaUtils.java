package com.taisau.substation.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/20 0020.
 */

public class FeaUtils {

    public static boolean saveFea(String path, float[] modFea) {
        File file = new File(path);
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            // create file output stream
            fos = new FileOutputStream(path);
            // create data output stream
            dos = new DataOutputStream(fos);
            // for each byte in the buffer
            for (float b : modFea) {
                // write float to the data output stream
                dos.writeFloat(b);
            }
            // force bytes to the underlying stream
            dos.flush();
            // create file input stream
        } catch (Exception e) {
            // if any I/O error occurs
            e.printStackTrace();
        } finally {
            // releases all system resources from the streams
            try {

                if (dos != null)
                    dos.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public static float[] readFea(String path) {
        float[] fea = new float[/*256*/512];
        InputStream is = null;
        DataInputStream dis = null;
        try {
            is = new FileInputStream(path);
            // create new data input stream
            dis = new DataInputStream(is);
            int i = 0;
            while (dis.available() > 0) {
                // read character
                float c = dis.readFloat();
                // print
                fea[i] = c;
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (dis != null)
                    dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return fea;
        // read till end of the stream
    }

    public static float[] getFeaFromFile(String feaPath) {
        if (feaPath == null) {
            return null;
        }
        return readFea(feaPath);
    }

    public static List<float[]> getFeaList(List<String> feaPaths) {
        List<float[]> modFeaList = new ArrayList<>();
        for (String feaPath : feaPaths) {
            float[] fea = getFeaFromFile(feaPath);
            if (fea != null) {
                modFeaList.add(fea);
            }
        }
        return modFeaList;
    }

    private static float[] feaD = new float[512], feaA = new float[512];

    public static float feaCompare(float[] fea1, float[] fea2) {
        float totalA = 0;
        float totalD = 0;
        for (int i = 0; i < 512; i++) {
            feaD[i] = fea1[i] - fea2[i];
            feaA[i] = fea1[i] + fea2[i];
            totalD += Math.abs(feaD[i]);
            totalA += Math.abs(feaA[i]);
        }
        float bi = totalD / totalA;
        return 120 - 80 * bi;
    }


    public static byte[] float2Byte(float[] inData) {
        int j = 0;
        int length = inData.length;
        byte[] outData = new byte[length * 4];
        for (float anInData : inData) {
            int data = Float.floatToIntBits(anInData);
            outData[j++] = (byte) (data >>> 24);
            outData[j++] = (byte) (data >>> 16);
            outData[j++] = (byte) (data >>> 8);
            outData[j++] = (byte) (data);
        }
        return outData;
    }

    public static byte[] encode(float floatArray[]) {
        byte byteArray[] = new byte[floatArray.length * 4];
        // wrap the byte array to the byte buffer
        ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
        // create a view of the byte buffer as a float buffer
        FloatBuffer floatBuf = byteBuf.asFloatBuffer();
        // now put the float array to the float buffer,
        // it is actually stored to the byte array
        floatBuf.put(floatArray);
        return byteArray;
    }


    public static float[] decode(byte byteArray[]) {
        float floatArray[] = new float[byteArray.length / 4];
        // wrap the source byte array to the byte buffer
        ByteBuffer byteBuf = ByteBuffer.wrap(byteArray);
        // create a view of the byte buffer as a float buffer
        FloatBuffer floatBuf = byteBuf.asFloatBuffer();
        // now get the data from the float buffer to the float array,
        // it is actually retrieved from the byte array
        floatBuf.get(floatArray);
        return floatArray;
    }

}
