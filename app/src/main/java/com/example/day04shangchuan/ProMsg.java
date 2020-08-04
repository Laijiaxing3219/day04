package com.example.day04shangchuan;

public class ProMsg {
    private int type;//类别
    private int progress;//当前进度
    private int max;//最大进度

    public ProMsg(int type, int progress, int max) {
        this.type = type;
        this.progress = progress;
        this.max = max;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
