package com.sensetoolbox.six.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class StructInputEvent {
	public final long timeval_sec;
	public final long timeval_usec;
	public final short type;
	public final String type_name;
	public final short code;
	public final String code_name;
	public final int value;
	
	private ByteBuffer byteArrayToBuffer(byte[] arr) {
		ByteBuffer bb = ByteBuffer.wrap(arr);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb;
	}
	
	public StructInputEvent(byte[] rawInput) {
		byte[] timeval_sec_array = new byte[4];
		byte[] timeval_usec_array = new byte[4];
		byte[] type_array = new byte[2];
		byte[] code_array = new byte[2];
		byte[] value_array = new byte[4];
		
		System.arraycopy(rawInput, 0, timeval_sec_array, 0, 4);
		System.arraycopy(rawInput, 4, timeval_usec_array, 0, 4);
		System.arraycopy(rawInput, 8, type_array, 0, 2);
		System.arraycopy(rawInput, 10, code_array, 0, 2);
		System.arraycopy(rawInput, 12, value_array, 0, 4);
		
		this.timeval_sec = byteArrayToBuffer(timeval_sec_array).getInt();
		this.timeval_usec = byteArrayToBuffer(timeval_usec_array).getInt();
		this.type = byteArrayToBuffer(type_array).getShort();
		this.code = byteArrayToBuffer(code_array).getShort();
		this.value = byteArrayToBuffer(value_array).getInt();
		
		switch (this.type) {
			case 0x00: this.type_name = "EV_SYN"; break;
			case 0x01: this.type_name = "EV_KEY"; break;
			case 0x02: this.type_name = "EV_REL"; break;
			case 0x03: this.type_name = "EV_ABS"; break;
			case 0x04: this.type_name = "EV_MSC"; break;
			case 0x05: this.type_name = "EV_SW"; break;
			case 0x11: this.type_name = "EV_LED"; break;
			case 0x12: this.type_name = "EV_SND"; break;
			case 0x14: this.type_name = "EV_REP"; break;
			case 0x15: this.type_name = "EV_FF"; break;
			case 0x16: this.type_name = "EV_PWR"; break;
			case 0x17: this.type_name = "EV_FF_STATUS"; break;
			case 0x1f: this.type_name = "EV_MAX"; break;
			default: this.type_name = "UNKNOWN (" + String.valueOf(this.type) + ")";
		}
		
		switch (this.code) {
			case 0x0b: this.code_name = "SYN_ELEMENTALX"; break;
		
			case 0x00: this.code_name = "SYN_REPORT"; break;
			case 0x01: this.code_name = "SYN_CONFIG"; break;
			case 0x02: this.code_name = "SYN_MT_REPORT"; break;
			case 0x03: this.code_name = "SYN_DROPPED"; break;
			case 0x0f: this.code_name = "SYN_MAX"; break;
			case 0x10: this.code_name = "SYN_CNT"; break;
			
			case 0x2f: this.code_name = "ABS_MT_SLOT"; break;
			case 0x30: this.code_name = "ABS_MT_TOUCH_MAJOR"; break;
			case 0x31: this.code_name = "ABS_MT_TOUCH_MINOR"; break;
			case 0x32: this.code_name = "ABS_MT_WIDTH_MAJOR"; break;
			case 0x33: this.code_name = "ABS_MT_WIDTH_MINOR"; break;
			case 0x35: this.code_name = "ABS_MT_POSITION_X"; break;
			case 0x36: this.code_name = "ABS_MT_POSITION_Y"; break;
			case 0x39: this.code_name = "ABS_MT_TRACKING_ID"; break;
			case 0x3a: this.code_name = "ABS_MT_PRESSURE"; break;
			default: this.code_name = "UNKNOWN (" + String.valueOf(this.code) + ")";
		}
	}
}