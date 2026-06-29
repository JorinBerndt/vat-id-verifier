package com.blockchain.vies;

public class VatResponse {
    private Boolean valid;
    private String name;
    private String address;
    private String error;
    private String vatId;

    // Constructor für Erfolg
    public VatResponse(Boolean valid, String vatId, String name, String address) {
        this.valid = valid;
        this.vatId = vatId;
        this.name = name;
        this.address = address;
    }

    // Constructor für Fehler
    public VatResponse(Boolean valid, String error) {
        this.valid = valid;
        this.error = error;
    }

    public Boolean getValid()   { return valid; }
    public String getName()     { return name; }
    public String getAddress()  { return address; }
    public String getError()    { return error; }
    public String getVatId()    { return vatId; }
}
