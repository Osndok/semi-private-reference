package com.allogy.spr;

/**
 * Created by robert on 2015-10-25 13:52.
 */
public
class Spr1Encryption
{
	private
	byte[] key;

	public
	Spr1Encryption(byte[] privateHash)
	{
		this.key=privateHash;
	}

	public
	Spr1Encryption(Spr1Key spr1Key)
	{
		this.key=spr1Key.getPrivateBytes();
	}

	public
	byte[] encrypt(byte[] bytes)
	{
		//TODO
		return null;
	}
}
