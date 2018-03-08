package com.example.alan.smartvanity;

/**
 * Created by Alan on 3/6/2018.
 */

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class WidgetHolder implements Serializable {

    // any other data you want
    public int id;

    public int width, height;

    public WidgetHolder() {}

    public WidgetHolder(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public static WidgetHolder deserialize( String data ) {
        try {
            byte [] bites = Base64.decode( data.getBytes(), 0);
            ByteArrayInputStream bis = new ByteArrayInputStream( bites );
            ObjectInputStream ois = new ObjectInputStream( bis );
            WidgetHolder holder = (WidgetHolder) ois.readObject();
            ois.close();
            return holder;
        } catch (IOException e ) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new WidgetHolder();
    }

    public String serialize() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream( baos );
            oos.writeObject(this);
            oos.flush();
            return new String(Base64.encode(baos.toByteArray(), 0));
        } catch( IOException e ) {
            return "";
        }
    }
}