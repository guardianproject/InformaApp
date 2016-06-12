package org.witness.informacam.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPCompressedData;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPLiteralData;
import org.spongycastle.openpgp.PGPObjectFactory;
import org.spongycastle.openpgp.PGPOnePassSignature;
import org.spongycastle.openpgp.PGPOnePassSignatureList;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSignatureList;
import org.spongycastle.openpgp.PGPUtil;
import org.witness.informacam.InformaCam;
import org.witness.informacam.models.credentials.ISecretKey;
import org.witness.informacam.models.j3m.ILogPack;
import org.witness.informacam.utils.Constants.Models.IUser;
import org.witness.informacam.utils.Constants.App.Crypto;
import org.witness.informacam.utils.Constants.App.Crypto.Signatures;

import android.content.Context;
import android.util.Log;

public class SignatureService {

	private PGPSecretKey secretKey = null;
	private PGPPrivateKey privateKey = null;
	private PGPPublicKey publicKey = null;
	private String authKey = null;

	private static String LOG = Crypto.LOG;

	public SignatureService (Context context) {}

	@SuppressWarnings({"deprecation" })
	public void initKey(ISecretKey sk) throws PGPException, GeneralSecurityException, IOException {
        // if users had a legacy authToken, fix this.  Somewhat of a horse-out-of-the-barn situation, but it helps.
        if(KeyUtility.checkForLegacyAuthToken(sk.secretAuthToken)) {
            sk.secretAuthToken = KeyUtility.wrapSecretAuthToken(sk.secretAuthToken);

            InformaCam.getInstance().ioService.saveBlob(
                    sk.asJson().toString().getBytes(),
                    new info.guardianproject.iocipher.File(IUser.SECRET));
        }

		// decrypt secretAuthToken with Android Keystore first.
		authKey = KeyUtility.unwrapSecretAuthToken(sk.secretAuthToken);
		secretKey = KeyUtility.extractSecretKey(sk.secretKey.getBytes());
		privateKey = secretKey.extractPrivateKey(authKey.toCharArray(), new BouncyCastleProvider());
		publicKey = secretKey.getPublicKey();

		sk = null;
	}

	@SuppressWarnings("deprecation")
	public boolean isVerified(final ILogPack data) throws IOException {

		try
		{
			byte[] signedData = (byte[]) data.remove(Signatures.Keys.SIGNATURE);
			ByteArrayInputStream sd = new ByteArrayInputStream(signedData);

			InputStream is = PGPUtil.getDecoderStream(sd);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			PGPObjectFactory objFactory = new PGPObjectFactory(is);
			PGPCompressedData cd = (PGPCompressedData) objFactory.nextObject();

			objFactory = new PGPObjectFactory(cd.getDataStream());

			PGPOnePassSignatureList sigList_o = (PGPOnePassSignatureList) objFactory.nextObject();
			PGPOnePassSignature sig = sigList_o.get(0);

			PGPLiteralData ld = (PGPLiteralData) objFactory.nextObject();
			InputStream literalIn = ld.getInputStream();

			sig.initVerify(publicKey, new BouncyCastleProvider());

			int read;
			while((read = literalIn.read()) > 0) {
				sig.update((byte) read);
				baos.write(read);
			}

			PGPSignatureList sigList = (PGPSignatureList) objFactory.nextObject();

			if(sig.verify(sigList.get(0)) && data.toString().equals(new String(baos.toByteArray()))) {
				baos.close();
				return true;
			} else {
				baos.close();
				return false;
			}

		}
		catch (PGPException e)
		{
			Log.d(LOG,"SignatureException: " + e.getMessage(),e);
			return false;
		} catch (SignatureException e) {
			Log.d(LOG,"SignatureException: " + e.getMessage(),e);
			return false;
		}

	}

	public void signData(InputStream is, OutputStream os) throws NoSuchAlgorithmException, SignatureException, PGPException, IOException {
		KeyUtility.applySignature(is, os, secretKey, publicKey, privateKey);
	}

	public byte[] signData(final byte[] data) throws NoSuchAlgorithmException, SignatureException, PGPException, IOException {
		return KeyUtility.applySignature(data, secretKey, publicKey, privateKey);
	}
}
