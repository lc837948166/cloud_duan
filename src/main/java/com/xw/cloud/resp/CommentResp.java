package com.xw.cloud.resp;

public class CommentResp<T> {
    boolean success;
    Object content;
    String msg;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public CommentResp(boolean success, Object content, String msg) {
        this.success = success;
        this.content = content;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "CommentResp{" +
                "success=" + success +
                ", content=" + content +
                ", msg='" + msg + '\'' +
                '}';
    }
}
