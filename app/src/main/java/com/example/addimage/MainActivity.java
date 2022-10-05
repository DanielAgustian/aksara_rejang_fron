package com.example.addimage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    protected static String [] aksara = {"ka", "ga", "nga","ta" ,"da","na","pa","ba","ma", "ca",
            "ja", "nya", "sa","ra", "la","wa","ya","ha","mba", "nda", "nja", "ngga", "a"};
    Button btn;
    ImageView imageView;
    EditText etAksara;
    Uri mImageUri;
    Double [] bebanw1 = new Double[37];
    Double [][] mpp = new Double [1][36];
    Double[][] arrayw1 = new Double [36][37];
    Double [] meanX = new Double [36];
    Double [] stdX = new Double [36];
    int i = 0 , j,k;
    Double stdAns, meanAns;
    Double mean, std;
    String data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.mButton);
        //btnTestDB = findViewById(R.id.btnTestDB);
        imageView = findViewById(R.id.mImageView);
        etAksara = findViewById(R.id.etAksara);
        CRUDFirebase();
    }

    public void infoKaganga(View v){
        startActivity(new Intent(MainActivity.this, BeginActivity.class));

    }

    public void onChooseFile (View v){
        CropImage.activity().start(MainActivity.this);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                mImageUri = result.getUri();
                try {
                   Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                   //imageView.setImageBitmap(bmp);
                   PreProcessing(bmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //imageView.setImageURI(mImageUri);
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e = result.getError();
                Toast.makeText(this, "Possible error is "+e,Toast.LENGTH_SHORT ).show();
            }
        }
    }
    public void PreProcessing(Bitmap bmp){
        int width = bmp.getWidth(), height = bmp.getHeight(),a=0;
        int [] pixels = new int [width*height];
        for (int y = 0 ; y<height; y++){
            for (int x = 0; x<width; x++){
                int index = (y * width) + x;
                int color = bmp.getPixel(x,y);
                 a = Color.alpha(color);
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                pixels[index] = (red+green+blue)/3;
                if(pixels[index] <= 127){
                    pixels[index]= 0;
                }
                else if (pixels[index] >127){
                    pixels[index]= 255;
                }

            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, ARGB_8888);
        for (int y = 0 ; y<height; y++){
            for (int x = 0; x<width; x++) {
                int index = (y * width) + x;
                bitmap.setPixel(x, y, Color.argb(a, pixels[index], pixels[index], pixels[index]));
            }
        }
        imageView.setImageBitmap(bitmap);
        MatrixPopulasiPixel(pixels,width,height);
    }
    public void MatrixPopulasiPixel(int []pixels, int width, int height){
        int ordo =6;
        int [] bagianRow = new int[8];
        int [] bagianColumn = new int[8];
        double jumlahRow = width/ordo;
        double jumlahColumn = height/ordo;
        for (int i=0;i<ordo+1;i++){
            if(i==0){
                bagianColumn[i]= 0;
                bagianRow[i]=0;
            }
            else{
                bagianColumn[i] = (int)Math.round(jumlahColumn*i);
                bagianRow[i] = (int) Math.round(jumlahRow*i);
            }
        }
        int index = 0;
        Double [] valuePop = new Double[36];
        for (int j =0;j<ordo;j++){
            for(int i = 0; i<ordo;i++){
                int allBlack =0,allPixel =0;
                index++;
                for (int y = bagianColumn[j]; y<bagianColumn[j+1];y++){
                    for(int x = bagianRow[i]; x<bagianRow[i+1];x++){
                        int koordinat =(y * width) + x;
                        if (pixels[koordinat]== 0){
                            allBlack = allBlack+1;
                        }
                        allPixel=allPixel+1;
                    }
                }
                Log.d("Debug mpp", index+". Black"+allBlack+"/"+allPixel);
                valuePop[index-1]= (double)allBlack/allPixel;
            }
        }

        Log.d("MPP MEan and stdX", ""+meanX[0]+" , "+stdX[0]);

        for (int var = 0; var< valuePop.length; var++){
            mpp[0][var] = (valuePop[var]-meanX[var])/stdX[var];
        }

        DotProduct();
        Log.d("Debugger", index+"");

    }

    public void CRUDFirebase(){
        i=0;
        List<Double[]> myList = new ArrayList<Double[]>();
        while(i<36){
            Log.d("GetWeight1", String.valueOf(i));
            GetWeight1(i);
            i= i+1;
        }
        k=0;
        while(k<37){
            Log.d("GetWeight2", String.valueOf(k));
            GetWeight2(k);
            k=k+1;
        }
        Log.d("Get STD Mean","Begin=====================");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("skripsi");
        DatabaseReference myRefData = myRef.child("Data");
        myRefData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Double [] tempMean = new Double[36];
                    Double [] tempStd = new Double[36];
                    for (int i =0; i<36; i++){
                        String init =String.valueOf(i);
                        String mean = dataSnapshot.child("Mean").child(init).getValue().toString();
                        String std = dataSnapshot.child("STD").child(init).getValue().toString();
                        Double dStd = Double.parseDouble(std);
                        Double dMean = Double.parseDouble(mean);
                        tempMean [i] = dMean;
                        tempStd[i] = dStd;
                        Log.d("std ke-"+i, ""+tempStd[i]);
                        Log.d("mean ke-"+i, ""+tempMean[i]);
                    }
                    Log.d("STDX Isi",""+tempStd[0]);
                    Log.d("MeanX Isi",""+tempMean[0]);
                    DataCongregationX(tempMean, 1);
                    DataCongregationX(tempStd, 2);
                    String meanY = dataSnapshot.child("MeanY").getValue().toString();
                    String stdY = dataSnapshot.child("StdY").getValue().toString();
                    Double dMeanY = Double.parseDouble(meanY);
                    Double dStdY = Double.parseDouble(stdY);
                    //Log.d("Mean STD", mean+" , "+std);

                    if(dMeanY!= null){
                        DataCongregation(dMeanY, 3);
                    }
                    if(dStdY!=null){
                        DataCongregation(dStdY, 4);
                    }

                }catch(Exception e){
                    Log.e("Mean Error", "Data cant be retrieved",e);
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


    }
    public String tostring(Object object){
        return object.toString();
    }
    public Double[] GetWeight1(int i){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("skripsi");
        DatabaseReference myRef1 = myRef.child("w1");
        DatabaseReference myRef11 = myRef1.child(Integer.toString(i));
        Log.d("GetWeight i", String.valueOf(i));
        myRef11.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                GenericTypeIndicator<List<Double>> gType = new GenericTypeIndicator<List<Double>>() {
                };

                List weight = dataSnapshot.getValue(gType);
                if(weight!=null){
                    Toast.makeText(MainActivity.this, "Data Retrieved", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.e("Data Empty", "Data failed to loaded");
                }
                Log.d("Value Data", "Value is: " + weight.get(0));
                Log.d("Size", String.valueOf(weight.size()));
                //for (int k = 0; k<36;k++){
                for (int j = 0; j<weight.size(); j++){
                    String a= tostring(weight.get(j));
                    Log.d("String a",a);
                    try {
                        Double d = Double.parseDouble(a);
                        Log.d("Single Array ["+j+"]", String.valueOf(d));
                        if (d!=null){
                            bebanw1[j] = d;
                        }
                    }catch(Exception e){
                        Log.e("Array Convert Error", "Error in beban", e);
                        e.printStackTrace();
                    }
                }
                try {
                    congergationW1(bebanw1);
                }catch(Exception e){
                    Log.e(TAG, "GetDataToOTherClass", e);
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        return bebanw1;
    }
    public void GetWeight2(int k1){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("skripsi");
        DatabaseReference myRef2 = myRef.child("w2");
        DatabaseReference myRef22 = myRef2.child(Integer.toString(k1));
        Log.d("GetWeight2 k", String.valueOf(k1));
        ValueEventListener listener = new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Double>> gType2 = new GenericTypeIndicator<List<Double>>() {
                };
                try {
                    List weight2 = dataSnapshot.getValue(gType2);
                    Log.d("Value W2", "Ukuran: "+weight2.get(0));
                    Double data = Double.parseDouble(String.valueOf(weight2.get(0)));
                    Log.d("Value W2",String.valueOf(data));
                    if(data!=null){
                        try {
                            Log.d("varK", ""+k);
                            congergationW2(data, k);
                        }catch(Exception e){
                            Log.e(TAG, "GetDataToOtherClass", e);
                        }

                    }

                }catch(Exception e){
                    Log.e("Error", "Data cant be retrieved");
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        };
        myRef22.addValueEventListener(listener);



    }

    int varCong1=0;
    public void congergationW1(Double [] data){

        for (int var=0; var<37; var++){
            if(data[var]!=null){
                arrayw1[varCong1][var]= data[var];
            }
        }
        Log.d("CongerW1 ke-"+varCong1, String.valueOf(arrayw1[varCong1][0])+" \n=============");
        varCong1 = varCong1+1;
        if(varCong1 == 36){
            Log.d("CongW1", arrayw1[0][0]+"");
        }
        if(varCong1>37){
            varCong1 = 0;
        }
    }
    Double [][] arrayw2 = new Double[37][1];
    int varCong = 0;
    public void congergationW2(Double data, int k){
        arrayw2[varCong][0] = data;
        Log.d("CongerW2 ke-"+varCong, String.valueOf(arrayw2[varCong][0])+" \n=============");
        varCong = varCong +1;
        if(varCong==37){
            varCong = 0;
            Log.d("Congw2",arrayw2[0][0]+"");
        }
    }
    public void DataCongregation(Double data, int kode){

        if(kode == 1){
            mean = data;
            Log.d("DataCongregation Mean",""+data);
            setMean(data);
        }
        else if(kode == 2){
            std = data;
            Log.d("DataCongregation STD",""+data);
            setStd(data);
        }else if(kode == 0){
            Log.e("Data Error","Value Null");
        }
        else if(kode == 3){
            meanAns = data;
            Log.d("DataCongregation MeanY",""+meanAns);
        }
        else if(kode ==4){
            stdAns = data;
            Log.d("DataCongregation stdY", ""+stdAns);
        }
    }
    public void DataCongregationX(Double [] data, int kode){
        if(kode == 1){
            for (int i =0; i<36; i++){
                meanX[i] = data[i];
                Log.d("DataCongregationX Mean", ""+meanX[i]);
            }
        }
        if(kode == 2){
            for (int i =0; i<36; i++){
                stdX[i] = data[i];
                Log.d("DataCongregationX STD", ""+stdX[i]);
            }
        }
    }
    public Double sigmoid(Double x){
        return 1 / (1 + Math.exp(-x));
    }



    int agree=0;
    public void DotProduct(){
        Double [][] result = new Double[1][37];
        Double result2= 0.0;
        for (int i=0; i<1; i++){
            for (int j=0; j<37;j++){
                result[i][j]=0.0000;
            }
        }

        if(arrayw1[0][0]!= null && arrayw2[0][0]!=null && mpp[0][0] != null){
            try {
                for (int i = 0; i < 1; i++) {
                    for (int j = 0; j < 37; j++) {
                        for (int k = 0; k < 36; k++) {
                            result[i][j] += mpp[i][k] * arrayw1[k][j];
                        }
                    }
                }
                Log.d("DotResult", result[0][0]+"");
            }catch(Exception e){
                Log.e("Dot Product", "Calculation Fault", e);
            }
            try {
                for (int i = 0; i < result.length; i++) {
                    for (int j = 0; j < result[0].length; j++){
                        result[i][j] = sigmoid(result[i][j]);
                    }
                }
            }catch(Exception e){
                Log.e("Dot Product","Sigmoid Calculation Fault", e);
            }
            for(int i = 0; i< result.length; i++){
                for (int j = 0; j<arrayw2[0].length; j++){
                    for (int k = 0; k < result[0].length; k++) {
                        result2 += result[i][k]*arrayw2[k][j];
                    }
                }
            }
            result2 = sigmoid(result2);
            Log.d("DotSigmoid2", result2+"");
            Result(result2);
        }
        else
        { DotProduct();}
    }

    public void Result(Double result){

        Double hasil = 0.00;
        hasil = (result *stdAns)+meanAns;
        Log.d("Result", hasil+"");
        int cariAksara = (int) Math.round(hasil);
        cariAksara = cariAksara - 1;
        data = aksara[cariAksara];
        Log.d("Result", "Aksara: "+data);
        etAksara.setText(data);
        //etAksara.setEnabled(false);
    }
    public void TampilHasil(View v){
        etAksara.setText(data);
    }
    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Double getStd() {
        return std;
    }

    public void setStd(Double std) {
        this.std = std;
    }

}
