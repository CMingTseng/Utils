package idv.neo.data;

import com.google.gson.annotations.SerializedName;

import idv.neo.utils.GsonConvertUtils;

/**
 * Created by Neo on 2017/4/21.
 */

public class MessageInfo {
    public final static int IDENTIFY = 0;
    public final static int SENSOR = 1;
    public final static int TEXT = 2;
    public final static String MESSAGE_IDENTIFY_SERVER = "SERVER";
    public final static String MESSAGE_IDENTIFY_SERVER_HELLO = "HELLO";
    public final static String MESSAGE_IDENTIFY_CLIENT = "MASTER";
    public final static String MESSAGE_IDENTIFY_SERVER_PW = "SPW";
    public final static String MESSAGE_SENSOR = "SENSOR";
    public final static String MESSAGE_TEXT = "TEXT";
    /**
     * type : 0 typename : YOOOO from : 192.168.1.2 to : 192.168.1.30 typevalue : {"sensortype":0,"sensorname":"YOOOO","sensorvalue":[{"valuename":"yyyy","valuequantity":12.5555},{"valuename":"yyyy","valuequantity":12.5555}]}
     */
    @SerializedName("type")
    private int mType;
    @SerializedName("typename")
    private String mTypename;
    @SerializedName("typevalue")
    private String mTypevalue;
    @SerializedName("from")
    private String mFrom;
    @SerializedName("to")
    private String mTo;

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getTypename() {
        return mTypename;
    }

    public void setTypename(String typename) {
        mTypename = typename;
    }

    public String getTypevalue() {
        return mTypevalue;
    }

    public void setTypevalue(String typevalue) {
        mTypevalue = typevalue;
    }

    public MessageInfo(int type, String name, String value) {
        this.mType = type;
        this.mTypename = name;
        this.mTypevalue = value;
    }

    public MessageInfo(int type, String name, String from, String to, String value) {
        this.mType = type;
        this.mTypename = name;
        this.mFrom = from;
        this.mTo = to;
        this.mTypevalue = value;
    }

    @Override
    public String toString() {
        return GsonConvertUtils.typetoJSONString(this);
    }
}
