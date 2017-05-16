package idv.neo.data;

import com.google.gson.annotations.SerializedName;

import idv.neo.utils.GsonConvertUtils;


/**
 * Created by Neo on 2017/4/21.
 */

public class MessageData {
    /**
     * data : {"type":0,"typename":"YOOOO","typevalue":{"sensortype":0,"sensorname":"YOOOO","sensorvalue":[{"valuename":"yyyy","valuequantity":12.5555},{"valuename":"yyyy","valuequantity":12.5555}]}}
     */

    @SerializedName("data")
    private MessageInfo mData;

    public MessageInfo getData() {
        return mData;
    }

    public void setData(MessageInfo data) {
        mData = data;
    }

    public MessageData(MessageInfo info) {
        this.mData = info;
    }

    public MessageData() {
    }

    @Override
    public String toString() {
        return GsonConvertUtils.typetoJSONString(this);
    }
}
