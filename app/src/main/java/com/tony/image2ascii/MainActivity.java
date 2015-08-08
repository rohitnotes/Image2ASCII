package com.tony.image2ascii;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "ABCDEFG";
    private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Image2ASCII/";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String ASCII="學第雄高科技大一ＥＯＶＬＩ－，　";
    private Button takePictureBtn;
    private ImageView img;
    private Uri mImageUri;
    private SimpleDateFormat sdf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //定義好時間字串的格式
        sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        File pathfile = new File(PATH);
        if(!pathfile.exists())
            pathfile.mkdirs();

        takePictureBtn = (Button)findViewById(R.id.takepicture);
        takePictureBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                //使用Intent調用其他服務幫忙拍照
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
        img = (ImageView)findViewById(R.id.img);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            //取出拍照後回傳資料
            Bundle extras = data.getExtras();
            //將資料轉換為圖像格式
            Bitmap sourceBitmap = (Bitmap) extras.get("data");
            //圖片放在手機上
            img.setImageBitmap(sourceBitmap);

            covert2ASCII(sourceBitmap);

        }
        //覆蓋原來的Activity
        super.onActivityResult(requestCode, resultCode, data);
    }

    //input source bitmap
    private void covert2ASCII(Bitmap sourceBitmap) {
        int i,j,x,y,h,w,a;
        h = sourceBitmap.getHeight();
        w = sourceBitmap.getWidth();
        Log.i(TAG,"h:"+h+" w:"+w);
        int[][] Y = new int[w][h]; //記錄每點的灰階值 [row][column]
        int[][] sum = new int[w/4][h/4]; // Compute the average gray value for each 4 by 4 block

        for(j=0;j<h;j++)
            for(i=0;i<w;i++) {
                int pixel = sourceBitmap.getPixel(i, j);
                int redValue = Color.red(pixel);
                int blueValue = Color.blue(pixel);
                int greenValue = Color.green(pixel);
                Y[i][j] = (int)(0.299*redValue+0.587*blueValue+0.114*greenValue);
                Y[i][j]=(int)(Y[i][j]*0.9);
            }

        for(j=0;j<h/4;j++)
            for(i=0;i<w/4;i++)
                sum[i][j]=0;

        for(j=0;j<h/4;j++)
            for(i=0;i<w/4;i++)
                for(y=0;y<4;y++)
                    for(x=0;x<4;x++)
                        sum[i][j]+=Y[i*4+x][j*4+y];

        for(j=0;j<h/4;j++)
            for(i=0;i<w/4;i++)
                sum[i][j]/=256;


        Log.i(TAG,"ASCII:"+ASCII);

        String dateformat = sdf.format(new Date());
        char[] charArray = ASCII.toCharArray();

        for(i=0;i<charArray.length;i++) {
            Log.i(TAG,"charArray["+i+"]:"+charArray[i]);
        }

        try {
            FileWriter fw = new FileWriter(PATH + dateformat + ".txt",false);
            BufferedWriter bw = new BufferedWriter(fw);
//            for(j = h/4-1 ; j >= 0 ; j--) {
            for(j = 0 ; j < h/4 ; j++) {
                bw.newLine();
                for (i = 0; i < w / 4; i++) {
                    a = sum[i][j];
                    char a1,a2;

                    if(2*a<=15) {
                        a1 = charArray[2 * a];
                        a2 = charArray[2 * a + 1];
                    } else {
                        a1 = charArray[14];
                        a2 = charArray[15];
                    }
                    bw.write(String.format("%c%c", a1, a2));
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
