package com.DST.scanlable.model;

public class TagInfo {
    private String epc;
    private int readCount;
    private String rssi;
    private int antenna;

    public TagInfo(String epc) {
        this.epc = epc;
        this.readCount = 1;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public void incrementReadCount() {
        this.readCount++;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public int getAntenna() {
        return antenna;
    }

    public void setAntenna(int antenna) {
        this.antenna = antenna;
    }
}