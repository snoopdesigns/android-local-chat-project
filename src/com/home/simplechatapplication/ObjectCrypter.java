package com.home.simplechatapplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ObjectCrypter {
	private Cipher deCipher;
	private Cipher enCipher;
	private SecretKeySpec key;
	private IvParameterSpec ivSpec;
	
	
	public ObjectCrypter(byte[] keyBytes,   byte[] ivBytes) {
		ivSpec = new IvParameterSpec(ivBytes);
	    try {
	         DESKeySpec dkey = new  DESKeySpec(keyBytes);
	         key = new SecretKeySpec(dkey.getKey(), "DES");
	         deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
	         enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (NoSuchPaddingException e) {
	        e.printStackTrace();
	    } catch (InvalidKeyException e) {
	        e.printStackTrace();
	    }
	}
	
	public byte[] encrypt(Object obj) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, ShortBufferException, BadPaddingException 
	{
	    byte[] input = convertToByteArray(obj);
	    enCipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
	    return enCipher.doFinal(input);
	}
	
	public Object decrypt( byte[]  encrypted) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException 
	{
	    deCipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
	    return convertFromByteArray(deCipher.doFinal(encrypted));
	}
	
	private Object convertFromByteArray(byte[] byteObject) throws IOException,ClassNotFoundException 
	{
	    ByteArrayInputStream bais;
	    ObjectInputStream in;
	    bais = new ByteArrayInputStream(byteObject);
	    in = new ObjectInputStream(bais);
	    Object o = in.readObject();
	    in.close();
	    return o;
	}

	private byte[] convertToByteArray(Object complexObject) throws IOException 
	{
	    ByteArrayOutputStream baos;
	    ObjectOutputStream out;
	    baos = new ByteArrayOutputStream();
	    out = new ObjectOutputStream(baos);
	    out.writeObject(complexObject);
	    out.close();
	    return baos.toByteArray();
	}
	public byte[] Encrypt(String str)
	{
		byte[] result = null;
		try {
			result = this.encrypt(str);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (ShortBufferException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	public String Decrypt(byte[] src)
	{
		String result = null;
		try {
			result = this.decrypt(src).toString();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}
}
