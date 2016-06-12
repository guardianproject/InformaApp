package org.witness.informacam.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.bcpg.BCPGOutputStream;
import org.spongycastle.bcpg.CompressionAlgorithmTags;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.bcpg.PublicKeyAlgorithmTags;
import org.spongycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.spongycastle.bcpg.sig.KeyFlags;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPCompressedData;
import org.spongycastle.openpgp.PGPCompressedDataGenerator;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPObjectFactory;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPPublicKeyRingCollection;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.PGPSecretKeyRingCollection;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPSignatureGenerator;
import org.spongycastle.openpgp.PGPSignatureList;
import org.spongycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.util.encoders.Hex;
import org.witness.informacam.InformaCam;
import org.witness.informacam.json.JSONArray;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.json.JSONTokener;
import org.witness.informacam.models.credentials.IKeyStore;
import org.witness.informacam.models.credentials.ISecretKey;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.storage.IOUtility;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.IManifest;
import org.witness.informacam.utils.Constants.Models.ICredentials;
import org.witness.informacam.utils.Constants.Models.IUser;

import android.os.Bundle;
import android.os.Message;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

public class KeyUtility {

	private final static String LOG = App.Crypto.LOG;

	public static String getFingerprintFromKey(byte[] keyblock) throws IOException, PGPException {
		PGPPublicKey key = extractPublicKeyFromBytes(keyblock);
		return new String(Hex.encode(key.getFingerprint()));
	}

	@SuppressWarnings("unchecked")
	public static PGPSecretKey extractSecretKey(byte[] keyblock) {
		PGPSecretKey secretKey = null;
		try {
			PGPSecretKeyRingCollection pkrc = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(Base64.decode(keyblock, Base64.DEFAULT))));
			Iterator<PGPSecretKeyRing> rIt = pkrc.getKeyRings();
			while(rIt.hasNext()) {
				PGPSecretKeyRing pkr = (PGPSecretKeyRing) rIt.next();
				Iterator<PGPSecretKey> kIt = pkr.getSecretKeys();
				while(secretKey == null && kIt.hasNext()) {
					secretKey = kIt.next();
				}
			}
			return secretKey;
		} catch(IOException e) {
			return null;
		} catch(PGPException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static PGPPublicKey extractPublicKeyFromBytes(byte[] keyBlock) throws IOException, PGPException {
		PGPPublicKeyRingCollection keyringCol = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(Base64.decode(keyBlock, Base64.DEFAULT))));
		PGPPublicKey key = null;
		Iterator<PGPPublicKeyRing> rIt = keyringCol.getKeyRings();
		while(key == null && rIt.hasNext()) {
			PGPPublicKeyRing keyring = (PGPPublicKeyRing) rIt.next();
			Iterator<PGPPublicKey> kIt = keyring.getPublicKeys();
			while(key == null && kIt.hasNext()) {
				PGPPublicKey k = (PGPPublicKey) kIt.next();
				if(k.isEncryptionKey())
					key = k;
			}
		}

		if(key == null) {
			throw new IllegalArgumentException("there isn't an encryption key here.");
		}

		return key;
	}

	public static String generatePassword(byte[] baseBytes) throws NoSuchAlgorithmException {
		// initialize random bytes
		byte[] randomBytes = new byte[baseBytes.length];
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.nextBytes(randomBytes);

		// xor by baseImage
		byte[] product = new byte[baseBytes.length];
		for(int b = 0; b < baseBytes.length; b++) {
			product[b] = (byte) (baseBytes[b] ^ randomBytes[b]);
		}

		// digest to SHA-256 string, voila password.
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return new String(Hex.encode(md.digest(product)));
	}

	@SuppressWarnings("deprecation")
	public static boolean initDevice() {

		int progress = 1;
        Bundle data = new Bundle();
		data.putInt(Codes.Extras.MESSAGE_CODE, Codes.Messages.UI.UPDATE);
		data.putInt(Codes.Keys.UI.PROGRESS, progress);

		InformaCam informaCam = InformaCam.getInstance();
		informaCam.update(data);

		informaCam.setCredentialManager(new CredentialManager(informaCam, !informaCam.ioService.isMounted(), true, true) {
			@Override
			public void onCacheWordUninitialized() {
				if(firstUse) {

					Log.d(LOG, "INIT: onCacheWordUninitialized()");

					try {
						setMasterPassword(informaCam.user.getString(IUser.PASSWORD).toCharArray());
					} catch (Exception e) {
						Log.e(LOG, e.toString());
						e.printStackTrace();
					}
				} else {
					super.onCacheWordUninitialized();
				}
			}


			@Override
			public void onCacheWordOpened() {
				// there is not credential block, so override this.
				if(firstUse) {
					Log.d(LOG, "INIT: onCacheWordOpened()");

					cacheWord.setTimeout(0);

					informaCam.ioService.initIOCipher(cacheWord.getEncryptionKey());

					new Thread ()
					{
						public void run ()
						{
							initDeviceAsync (informaCam, informaCam.getCredentialManager());
						}
					}.start();

				} else {
					super.onCacheWordOpened();
				}
			}
		});

		return true;
	}

	private static void initDeviceAsync (InformaCam informaCam, CredentialManager credMgr)
	{
		try {
			final String authToken;

			String basePath = informaCam.user.getJSONArray(IUser.PATH_TO_BASE_IMAGE).getString(0);
			byte[] baseImageBytes = informaCam.ioService.getBytes(basePath, Storage.Type.INTERNAL_STORAGE);

			authToken = generatePassword(baseImageBytes);

			String authTokenBlobBytes = new String(credMgr.setAuthToken(authToken));
			JSONObject authTokenBlob = (JSONObject) new JSONTokener(authTokenBlobBytes).nextValue();
			authTokenBlob.put(ICredentials.PASSWORD_BLOCK, authTokenBlob.getString("value"));
			authTokenBlob.remove("value");

			initDeviceKeys(authToken, baseImageBytes);

			if(informaCam.ioService.saveBlob(authTokenBlob.toString().getBytes(), new java.io.File(IUser.CREDENTIALS))) {
				informaCam.user.setHasCredentials(true);

			}

			informaCam.initData();

			for(String s : informaCam.getAssets().list("includedOrganizations")) {

				InputStream ictdIS = informaCam.ioService.getStream("includedOrganizations/" + s, Type.APPLICATION_ASSET);

				byte[] ictdBytes = new byte[ictdIS.available()];
				ictdIS.read(ictdBytes);


				IOrganization organization = informaCam.installICTD((JSONObject) new JSONTokener(new String(ictdBytes)).nextValue(), informaCam.h, informaCam);
				if(organization != null && !informaCam.user.isInOfflineMode) {

					/*
					INotification notification = new INotification(informaCam.getResources().getString(R.string.key_sent), informaCam.getResources().etResources().getString(R.string.you_have_sent_your_credentials_to_x, organization.organizationName), Models.INotification.Type.NEW_KEY);
					notification.taskComplete = false;
					informaCam.addNotification(notification, null);
					*/
				//	ITransportStub transportStub = new ITransportStub(organization, notification);
				//	transportStub.setAsset(IUser.PUBLIC_CREDENTIALS, IUser.PUBLIC_CREDENTIALS, MimeType.ZIP, Type.IOCIPHER);
				//	TransportUtility.initTransport(transportStub);
				}
			}


		try {
			for(String s : informaCam.getAssets().list("includedForms")) {
				InputStream formXML = informaCam.ioService.getStream("includedForms/" + s, Type.APPLICATION_ASSET);
				FormUtility.importAndParse(formXML);
			}
		} catch(Exception e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}


		// Tell others we are done!
		Bundle data = new Bundle();
		data.putInt(Codes.Extras.MESSAGE_CODE, org.witness.informacam.utils.Constants.Codes.Messages.UI.REPLACE);

		Message message = new Message();
		message.setData(data);

		informaCam.update(data);

		} catch (Exception e) {
			Log.e(LOG, e.toString(),e);
		}
	}

    public static String unwrapSecretAuthToken(String secretAuthToken) throws GeneralSecurityException, IOException {
        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        final KeyStore.PrivateKeyEntry pke = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ICredentials.PASSWORD_ALIAS, null);
        final KeyPair keyPair = new KeyPair(pke.getCertificate().getPublicKey(), pke.getPrivateKey());

        final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

        CipherInputStream cis = new CipherInputStream(
                new ByteArrayInputStream(Base64.decode(secretAuthToken.getBytes("UTF-8"), Base64.DEFAULT)), cipher);

        ArrayList<Byte> a_bytes = new ArrayList<Byte>();
        int next;
        while ((next = cis.read()) != -1) {
            a_bytes.add((byte) next);
        }

        byte[] bytes = new byte[a_bytes.size()];
        for(int b=0; b<bytes.length; b++) {
            bytes[b] = a_bytes.get(b).byteValue();
        }

        return new String(bytes, 0, bytes.length, "UTF-8");
    }

    public static boolean checkForLegacyAuthToken(String secretAuthToken) {
        if(secretAuthToken.length() <= 44) {
            return true;
        }

        return false;
    }

    public static String wrapSecretAuthToken(String secretAuthToken) throws GeneralSecurityException, IOException {
        // encrypt to Android Keystore first.

        final Calendar start = new GregorianCalendar();
        final Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 100);

        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        final KeyPairGeneratorSpec kpgSpec = new KeyPairGeneratorSpec.Builder(InformaCam.getInstance())
                .setAlias(ICredentials.PASSWORD_ALIAS)
                .setSubject(new X500Principal(String.format("CN=%s", ICredentials.PASSWORD_ALIAS)))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime()).build();

        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        kpg.initialize(kpgSpec);
        kpg.generateKeyPair();

        final KeyStore.PrivateKeyEntry pke = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ICredentials.PASSWORD_ALIAS, null);
        final KeyPair keyPair = new KeyPair(pke.getCertificate().getPublicKey(), pke.getPrivateKey());

        final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CipherOutputStream cos = new CipherOutputStream(baos, cipher);
        cos.write(secretAuthToken.getBytes("UTF-8"));
        cos.close();

        byte[] secretAuthTokenBytes = baos.toByteArray();
        baos.close();

        return Base64.encodeToString(secretAuthTokenBytes, Base64.DEFAULT);
    }

	private static boolean initDeviceKeys (String authToken, byte[] baseImageBytes)
	{
		InformaCam informaCam = InformaCam.getInstance();

		int progress = 1;
		Bundle data = new Bundle();
		data.putInt(Codes.Extras.MESSAGE_CODE, Codes.Messages.UI.UPDATE);
		data.putInt(Codes.Keys.UI.PROGRESS, progress);

        try {

            String secretAuthToken, keyStorePassword;

            progress += 10;
            data.putInt(Codes.Keys.UI.PROGRESS, progress);
			informaCam.update(data);

            secretAuthToken = generatePassword(baseImageBytes);
			keyStorePassword = generatePassword(baseImageBytes);

			// TODO: set up anonymization vector here

			baseImageBytes = null;

			progress += 10;
			data.putInt(Codes.Keys.UI.PROGRESS, progress);
			informaCam.update(data);


			progress += 10;
			data.putInt(Codes.Keys.UI.PROGRESS, progress);
			informaCam.update(data);

			Map<String, InputStream> publicCredentials = new HashMap<String, InputStream>();
			JSONArray baseImages = informaCam.user.getJSONArray(IUser.PATH_TO_BASE_IMAGE);
			for(int j=0; j<baseImages.length(); j++) {

				InputStream baseImageStream = informaCam.ioService.getStream(baseImages.getString(j), Storage.Type.INTERNAL_STORAGE);

				info.guardianproject.iocipher.File baseImage = new info.guardianproject.iocipher.File(IUser.BASE_IMAGE + "_" + j);
				if(informaCam.ioService.saveBlob(baseImageStream, baseImage)) {
					informaCam.ioService.delete(baseImages.getString(j), Storage.Type.INTERNAL_STORAGE);
					publicCredentials.put(IUser.BASE_IMAGE + "_" + j, informaCam.ioService.getStream(baseImage.getAbsolutePath(), Storage.Type.IOCIPHER));
				}
			}

			informaCam.user.remove(IUser.PATH_TO_BASE_IMAGE);

			progress += 10;
			data.putInt(Codes.Keys.UI.PROGRESS, progress);
			informaCam.update(data);

			Security.addProvider(new BouncyCastleProvider());
			KeyPairGenerator kpg;

			kpg = KeyPairGenerator.getInstance("RSA","BC");
			kpg.initialize(4096);
			KeyPair keyPair = kpg.generateKeyPair();

			progress += 10;
			data.putInt(Codes.Keys.UI.PROGRESS, progress);
			informaCam.update(data);

			PGPSignatureSubpacketGenerator hashedGen = new PGPSignatureSubpacketGenerator();
			hashedGen.setKeyFlags(true, KeyFlags.ENCRYPT_STORAGE);
			hashedGen.setPreferredCompressionAlgorithms(false, new int[] {
					CompressionAlgorithmTags.ZLIB,
					CompressionAlgorithmTags.ZIP
			});
			hashedGen.setPreferredHashAlgorithms(false, new int[] {
					HashAlgorithmTags.SHA256,
					HashAlgorithmTags.SHA384,
					HashAlgorithmTags.SHA512
			});
			hashedGen.setPreferredSymmetricAlgorithms(false, new int[] {
					SymmetricKeyAlgorithmTags.AES_256,
					SymmetricKeyAlgorithmTags.AES_192,
					SymmetricKeyAlgorithmTags.AES_128,
					SymmetricKeyAlgorithmTags.CAST5,
					SymmetricKeyAlgorithmTags.DES
			});
			progress += 10;
			data.putInt(Codes.Keys.UI.PROGRESS, progress);
			informaCam.update(data);

			PGPSecretKey secret = new PGPSecretKey(
					PGPSignature.DEFAULT_CERTIFICATION,
					PublicKeyAlgorithmTags.RSA_GENERAL,
					keyPair.getPublic(),
					keyPair.getPrivate(),
					new Date(),
					"InformaCam OpenPGP Key: " + informaCam.user.getString(IUser.ALIAS),
					SymmetricKeyAlgorithmTags.AES_256,
					secretAuthToken.toCharArray(),
					hashedGen.generate(),
					null,
					new SecureRandom(),
					"BC");

			String pgpKeyFingerprint = new String(Hex.encode(secret.getPublicKey().getFingerprint()));
			informaCam.user.pgpKeyFingerprint = pgpKeyFingerprint;

			ISecretKey secretKeyPackage = new ISecretKey();
			secretKeyPackage.pgpKeyFingerprint = pgpKeyFingerprint;
			secretKeyPackage.secretAuthToken = wrapSecretAuthToken(secretAuthToken);
            secretKeyPackage.secretKey = Base64.encodeToString(secret.getEncoded(), Base64.DEFAULT);

			progress += 10;
			data.putInt(Codes.Keys.UI.PROGRESS, progress);
			informaCam.update(data);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ArmoredOutputStream aos = new ArmoredOutputStream(baos);
			aos.write(secret.getPublicKey().getEncoded());
			aos.flush();
			aos.close();
			baos.flush();
			
			publicCredentials.put(IUser.PUBLIC_KEY, new ByteArrayInputStream(baos.toByteArray()));			
			baos.close();
			
			JSONObject credentials = new JSONObject();
			credentials.put(IUser.ALIAS, informaCam.user.getString(IUser.ALIAS));
			credentials.put(IUser.EMAIL, informaCam.user.getString(IUser.EMAIL));
			publicCredentials.put(IUser.CREDENTIALS, new ByteArrayInputStream(credentials.toString().getBytes()));

			IOUtility.zipFiles(publicCredentials, IUser.PUBLIC_CREDENTIALS, Type.IOCIPHER);

			progress += 10;
			data.putInt(Codes.Keys.UI.PROGRESS, progress);
			informaCam.update(data);

			if(informaCam.ioService.saveBlob(new byte[0], new info.guardianproject.iocipher.File(IManifest.KEY_STORE))) {
				// make keystore manifest
				IKeyStore keyStoreManifest = new IKeyStore();
				keyStoreManifest.password = keyStorePassword;
				keyStoreManifest.path = IManifest.KEY_STORE;
				keyStoreManifest.lastModified = System.currentTimeMillis();
				informaCam.saveState(keyStoreManifest);
				Log.d(LOG, "KEY STORE INITED");
			}
			progress += 10;
			data.putInt(Codes.Keys.UI.PROGRESS, progress);
			informaCam.update(data);

			if(informaCam.ioService.saveBlob(
					secretKeyPackage.asJson().toString().getBytes(), 
					new info.guardianproject.iocipher.File(IUser.SECRET))
					) {
				informaCam.user.alias = informaCam.user.getString(IUser.ALIAS);
				informaCam.user.email = informaCam.user.getString(IUser.EMAIL);

				informaCam.user.remove(IUser.AUTH_TOKEN);
				informaCam.user.remove(IUser.PATH_TO_BASE_IMAGE);
				informaCam.user.remove(IUser.ALIAS);
				informaCam.user.remove(IUser.EMAIL);
				informaCam.user.hasPrivateKey = true;

				informaCam.user.save();
				
				progress += 9;
				data.putInt(Codes.Keys.UI.PROGRESS, progress);
				informaCam.update(data);
			}

			return true;
		} catch (Exception e) {
			Log.e(LOG, e.toString(),e);
			
		} 

		return false;

	}
	
	@SuppressWarnings("deprecation")
	public static boolean verifySig(byte[] signature, byte[] data, PGPPublicKey publicKey) {
		BouncyCastleProvider bc = new BouncyCastleProvider();
		Security.addProvider(bc);
		
		ByteArrayInputStream bais_sig = new ByteArrayInputStream(signature);
		ByteArrayInputStream bais_data = new ByteArrayInputStream(data);
		
		try {
			InputStream is = PGPUtil.getDecoderStream(bais_sig);
			PGPObjectFactory objFactory = new PGPObjectFactory(is);
			
			PGPCompressedData cData1 = (PGPCompressedData) objFactory.nextObject();
			objFactory = new PGPObjectFactory(cData1.getDataStream());
			
			PGPSignatureList sigList = (PGPSignatureList) objFactory.nextObject();
			PGPSignature sig = sigList.get(0);
			sig.initVerify(publicKey, bc);
			
			int ch;
			while((ch = bais_data.read()) >= 0) {
				sig.update((byte) ch);
			}
			
			if(sig.verify()) {
				return true;
			}
			
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (PGPException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (SignatureException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		
		return false;
	}

	@SuppressWarnings({ "deprecation" })
	public static byte[] applySignature(byte[] data, PGPSecretKey secretKey, PGPPublicKey publicKey, PGPPrivateKey privateKey) throws NoSuchAlgorithmException, PGPException, IOException, SignatureException {
		BouncyCastleProvider bc = new BouncyCastleProvider();
		Security.addProvider(bc);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		OutputStream targetOut = new ArmoredOutputStream(baos);
		
		PGPSignatureGenerator sGen = new PGPSignatureGenerator(secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1, bc);
		sGen.initSign(PGPSignature.BINARY_DOCUMENT, privateKey);
		
		PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.ZLIB);
		BCPGOutputStream bOut = new BCPGOutputStream(cGen.open(targetOut));
		
		sGen.update(data);
		
		sGen.generate().encode(bOut);
		
		cGen.close();
		bOut.close();
		targetOut.close();
		
		byte[] outdata = baos.toByteArray();
		return outdata;

	
	}
	
	@SuppressWarnings({ "deprecation" })
	public static void applySignature(InputStream is, OutputStream os, PGPSecretKey secretKey, PGPPublicKey publicKey, PGPPrivateKey privateKey) throws NoSuchAlgorithmException, PGPException, IOException, SignatureException {
		BouncyCastleProvider bc = new BouncyCastleProvider();
		Security.addProvider(bc);
		
		OutputStream targetOut = new ArmoredOutputStream(os);
		
		PGPSignatureGenerator sGen = new PGPSignatureGenerator(secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1, bc);
		sGen.initSign(PGPSignature.BINARY_DOCUMENT, privateKey);
		
		PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.ZLIB);
		BCPGOutputStream bOut = new BCPGOutputStream(cGen.open(targetOut));
		
		byte[] buf = new byte[4096];
		int len;
		
		while ((len = is.read(buf)) > 0) {
		            sGen.update(buf, 0, len);
	
		}
		
		sGen.generate().encode(bOut);
		
		cGen.close();
		bOut.close();
		targetOut.close();
			
	}
	
	
}
