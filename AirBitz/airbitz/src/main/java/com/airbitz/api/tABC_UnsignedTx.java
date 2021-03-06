/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.airbitz.api;

public class tABC_UnsignedTx {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected tABC_UnsignedTx(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(tABC_UnsignedTx obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        coreJNI.delete_tABC_UnsignedTx(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setData(SWIGTYPE_p_void value) {
    coreJNI.tABC_UnsignedTx_data_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }

  public SWIGTYPE_p_void getData() {
    long cPtr = coreJNI.tABC_UnsignedTx_data_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public void setSzTxId(String value) {
    coreJNI.tABC_UnsignedTx_szTxId_set(swigCPtr, this, value);
  }

  public String getSzTxId() {
    return coreJNI.tABC_UnsignedTx_szTxId_get(swigCPtr, this);
  }

  public void setSzTxMalleableId(String value) {
    coreJNI.tABC_UnsignedTx_szTxMalleableId_set(swigCPtr, this, value);
  }

  public String getSzTxMalleableId() {
    return coreJNI.tABC_UnsignedTx_szTxMalleableId_get(swigCPtr, this);
  }

  public void setFees(SWIGTYPE_p_uint64_t value) {
    coreJNI.tABC_UnsignedTx_fees_set(swigCPtr, this, SWIGTYPE_p_uint64_t.getCPtr(value));
  }

  public SWIGTYPE_p_uint64_t getFees() {
    return new SWIGTYPE_p_uint64_t(coreJNI.tABC_UnsignedTx_fees_get(swigCPtr, this), true);
  }

  public void setCountOutputs(long value) {
    coreJNI.tABC_UnsignedTx_countOutputs_set(swigCPtr, this, value);
  }

  public long getCountOutputs() {
    return coreJNI.tABC_UnsignedTx_countOutputs_get(swigCPtr, this);
  }

  public void setAOutputs(SWIGTYPE_p_p_sABC_TxOutput value) {
    coreJNI.tABC_UnsignedTx_aOutputs_set(swigCPtr, this, SWIGTYPE_p_p_sABC_TxOutput.getCPtr(value));
  }

  public SWIGTYPE_p_p_sABC_TxOutput getAOutputs() {
    long cPtr = coreJNI.tABC_UnsignedTx_aOutputs_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_p_sABC_TxOutput(cPtr, false);
  }

  public tABC_UnsignedTx() {
    this(coreJNI.new_tABC_UnsignedTx(), true);
  }

}
