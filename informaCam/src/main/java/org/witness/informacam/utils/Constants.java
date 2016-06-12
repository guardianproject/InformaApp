package org.witness.informacam.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.witness.informacam.models.j3m.ILogPack;
import org.witness.informacam.models.media.IAsset;
import org.witness.informacam.models.transport.ITransportStub;
import org.witness.informacam.ui.editors.IRegionDisplay;

import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.android.gms.common.Scopes;

public class Constants {
	
	public interface IRegionDisplayListener {
		public void onSelected(IRegionDisplay regionDisplay);
		public int[] getSpecs();
	}

	public interface ModelListener {
		public void requestUpdate();
	}

	public interface InformaCamEventListener {
		public void onUpdate(Message message);
	}

	public interface WizardListener {
		public FragmentManager returnFragmentManager();
		public void wizardCompleted();
		public void onSubFragmentCompleted();
		public void onSubFragmentInitialized();
	}
	
	public interface SuckerCacheListener {
		public void onUpdate(long timestamp, ILogPack ILogPack);
		public long onUpdate(ILogPack ILogPack);
	}
	
	public interface HttpUtilityListener {
		public void onOrbotRunning();
	}
	
	public interface MetadataEmbededListener {
		public void onMetadataEmbeded(IAsset version);
		public void onMediaReadyForTransport(ITransportStub transportStub);
	}
	
	public interface VideoConstructorListener {
		public void onCommandFinished(java.io.File result);
	}
	
	public interface ListAdapterListener {
		public void updateAdapter(int which);
		public void setPending(int numPending, int numCompleted);
	}
	
	public final static class Logger {
		public Logger() {}
		
		public final static boolean DEBUG = true;
		
		public static void e(String LOG, ExceptionInInitializerError e) {
			Log.e(LOG, e.toString(),e);
			try {
				Log.e(LOG, e.getMessage());
			} catch(NullPointerException npe) {}
			
			e.printStackTrace();
		}
		
		public static void e(String LOG, Exception e) {
			Log.e(LOG, e.toString(),e);
			
		}
		
		public static void d(String LOG, String msg) {
			if (DEBUG)
				Log.d(LOG, msg);
		}
	}

	public final static class Actions {
		public final static String INIT = "org.witness.informacam.action.INIT";
		public final static String SHUTDOWN = "org.witness.informacam.action.SHUTDOWN";
		public final static String ASSOCIATE_SERVICE = "org.witness.informacam.action.ASSOCIATE_SERVICE";
		public static final String DISASSOCIATE_SERVICE = "org.witness.informacam.action.DISASSOCIATE_SERVICE";
		public static final String UPLOADER_UPDATE = "org.witness.informacam.action.UPLOADER_UPDATE";
		public static final String CAMERA = "android.media.action.IMAGE_CAPTURE";
		public static final String INFORMACAM_START = "org.witness.informacam.action.INFORMACAM_START";
		public static final String INFORMACAM_STOP = "org.witness.informacam.action.INFORMACAM_STOP";
		public static final String INFORMA_START = "org.witness.informacam.action.INFORMA_SERVICE_START";
		public static final String INFORMA_STOP = "org.witness.informacam.action.INFORMA_SERVICE_STOP";
		public static final String PERSISTENT_SERVICE = "org.witness.informacam.action.PERSISTENT_SERVICE";
		public static final String VERIFIED_MOBILE_MEDIA = "info.guardianproject.action.VERIFIED_MOBILE_MEDIA";
		public static final String USER_ACCEPT_ACTION = "org.witness.informacam.action.USER_ACCEPT_ACTION";
		
		public static final String[] OUTSIDE_THE_LOOP = new String[] {
			VERIFIED_MOBILE_MEDIA,
			CAMERA
		};
	}

	public final static class Codes {
		public final static class Routes {
			public final static int IMAGE_CAPTURE = 100;
			public final static int SIGNATURE_SERVICE = 101;
			public static final int IO_SERVICE = 102;
			public static final int UPLOADER_SERVICE = 103;
			public static final int RETRY_SAVE = 104;
			public static final int RETRY_GET = 105;
			public static final int INFORMA_SERVICE = 106;
			public static final int BACKGROUND_PROCESSOR = 107;
		}
		
		public final static class Authentication {
			public final static int REQUEST_ACCOUNT_PICKER = 200;
			public final static int REQUEST_AUTHORIZATION = 201;
		}
		
		public final static class Tasks {
			public final static int ANALYZE_MEDIA = 1;
		}
		
		public final static class Status {
			public final static int UNKNOWN = 0;
			public final static int UNINITIALIZED = 1;
			public final static int LOCKED = 2;
			public final static int UNLOCKED = 3;
		}

		public final static class Keys {
			public final static String SERVICE = "service";
			public static final String IV = "iv";
			public static final String VALUE = "value";
			public static final String UPLOADER = "uploader";
			public static final String DCIM_DESCRIPTOR = "dcimDescriptor";
			public static final String BATCH_EXPORT_FINISHED = "batchExportFinished";

			public static final class UI {
				public static final String PROGRESS = "progress";
				public static final String UPDATE = "update";
			}
		}

		public final static class Extras {
			public final static String WIZARD_SUPPLEMENT = "wizard_supplement";
			public static final String MESSAGE_CODE = "message_code";
			public static final String RETURNED_MEDIA = "informacam_returned_media";
			public static final String INSTALL_NEW_KEY = "install_ictd_uri";
			public static final String LOGOUT_USER = "logout_user";
			public static final String RESTRICT_TO_PROCESS = "restrict_to_process";
			public static final String CAMERA_TYPE = "camera_type";
			public static final String GPS_FAILURE = "gps_failure";
			public static final String SET_LOCALES = "set_locales";
			public static final String LOCALE_PREF_KEY = "locale_pref_key";
			public static final String CHANGE_LOCALE = "changeLocale";
			public static final String CONSOLIDATE_MEDIA = "consolidateMedia";
			public static final String MEDIA_PARENT = "mediaParent";
			public static final String GENERAL_FAILURE = "generalFailure";
			public static final String INFORMA_CACHE = "informaCacheFile";
			public static final String TIME_OFFSET = "informaTimeOffset";
			public static final String NUM_PROCESSING = "numProcessing";
			public static final String NUM_COMPLETED = "numCompleted";
			public static final String CRON_INTERVAL = "informaCronInterval";
		}

		public static final class Messages {
			public static final class Transport {
				public static final int GENERAL_FAILURE = 404;
				public static final int ORBOT_NOT_RUNNING = 405;
				public static final int ORBOT_UNINSTALLED = 406;
			}
			
			public static final class Wizard {
				public final static int INIT = 300;
			}

			public static final class UI {
				public final static int UPDATE = 301;
				public static final int REPLACE = 302;
			}

			public static final class Login {
				public final static int DO_LOGIN = 303;
				public final static int DO_LOGOUT = 304;
			}

			public static final class DCIM {
				public final static int START = 305;
				public final static int STOP = 306;
				public final static int ADD = 307;
				public final static int PENDING = 308;
			}

			public static final class Home {
				public final static int INIT = 309;
			}

		}

		public static final class Transport {

			public static final int MUST_INSTALL_TOR = 400;
			public static final int MUST_START_TOR = 401;
		}

		public class Media {
			public static final int ORIENTATION_PORTRAIT = 1;
			public static final int ORIENTATION_LANDSCAPE = 2;

			public static final int TYPE_IMAGE = 400;
			public static final int TYPE_VIDEO = 401;
			public static final int TYPE_JOURNAL = 402;
		}

		public class Adapters {
			public static final int ALL = 0;
			public static final int NOTIFICATIONS = 1;
			public static final int ORGANIZATIONS = 2;
		}
	}
	
	public final static class Time {
		public final static String LOG = " InformaCam: TIME ";

		public final static class DateFormats {
			public static final String EXPORT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
			public static final String EXIF_DATE_FORMAT = "yyyy:MM:dd HH:mm:ss";
		}

		public static final class Keys {

			public static final String RELATIVE_TIME = "mediaRelativeTimestamp";

		}
	}
	
	public final static class Forms {
		public final static String LOG = "InformaForms";
	}
	
	public final static class Ffmpeg {
		public final static String LOG = "InformaFFMPEG";
	}

	public final static class Suckers {
		public final static String LOG = "InformaSuckers";
		public static final int GPS_WAIT_MAX = 30;
		public static final int DEFAULT_CRON_INTERVAL = 45;
		public static final int DEFAULT_CRON_ACTIVE_INTERVAL = 1;

		public final static class CaptureEvent {
			public final static int METADATA_CAPTURED = 272;
			public final static int MEDIA_OPENED = 273;
			public final static int REGION_GENERATED = 274;
			public final static int MEDIA_SAVED = 275;
			public final static int SENSOR_PLAYBACK = 271;
			public final static int TIMESTAMPS_RESOLVED= 270;
			public final static int FORM_EDITED = 269;
			
			
			public final static class Keys {
				public final static String USER_ACTION = "userActionReported";
				public final static String TYPE = "captureTypes";
				public final static String MATCH_TIMESTAMP = "captureEventMatchTimestamp";
				public final static String TIMESTAMP = "captureEventTimestamp";
				public final static String ON_VIDEO_START = "timestampOnVideoStart";
				public final static String MEDIA_CAPTURE_COMPLETE = "mediaCapturedComplete";
				public final static String METADATA_CAPTURED = "metadataCaptured";
				public final static String REGION_LOCATION_DATA = "regionLocationData";
			}
		}

		public final static class Device {
            public final static long LOG_RATE = 10000L;

            public final static class Keys {
                public static final String FILE_EFFECTED = "fileEffected";
                public static final String ACCESS_TYPE = "accessType";
                public static final String ACCESS_CODE = "accessCode";
                public static final String PLUG_EVENT_TYPE = "plugEventType";
                public static final String PLUG_EVENT_CODE = "plugEventCode";
            }
        }

		public final static class Phone {
			public final static long LOG_RATE = 20000L;

			public final static class Keys {
				public static final String CELL_ID = "cellTowerId";
				public static final String BLUETOOTH_DEVICE_ADDRESS = "bluetoothDeviceAddress";
				public static final String BLUETOOTH_DEVICE_NAME = "bluetoothDeviceName";
				public static final String VISIBLE_WIFI_NETWORKS = "visibleWifiNetworks";
				public static final String BSSID = "bssid";
				public static final String SSID = "ssid";
				public static final String WIFI_FREQ = "wifiFreq";
				public static final String WIFI_LEVEL = "wifiLevel";
				public static final String BLUETOOTH_DEVICE_REDACTED = "bluetoothDeviceRedacted";
				public static final String LAC = "LAC";
				public static final String MCC = "MCC";
				public static final String MNC = "MNC";
			}
		}

		public final static class Accelerometer {
			public final static long LOG_RATE = 2000L; //let's lower this for now
			
			public final static class Keys {
				public static final String ACC = "acc";
				public static final String ORIENTATION = "orientation";
				public static final String X = "acc_x";
				public static final String Y = "acc_y";
				public static final String Z = "acc_z";
				public static final String PITCH = "pitch";
				public static final String ROLL = "roll";
				public static final String AZIMUTH = "azimuth";
				public static final String PITCH_CORRECTED = "pitchCorrected";
				public static final String ROLL_CORRECTED = "rollCorrected";
				public static final String AZIMUTH_CORRECTED = "azimuthCorrected";
				public static final String BEARING_DEGREES = "bearingDegrees";
			}
		}
		
		public final static class Environment {
			public final static long LOG_RATE = 1000L;
			
			public final static class Keys {
				public static final String AMBIENT_TEMP = "ambientTemperature";
				public static final String AMBIENT_TEMP_CELSIUS = "ambientTemperatureCelsius";
				
				public static final String DEVICE_TEMP = "deviceTemperature";
				public static final String DEVICE_TEMP_CELSIUS = "deviceTemperatureCelsius";
				
				public static final String HUMIDITY = "relativeHumidity";
				public static final String HUMIDITY_PERC = "relativeHumidityPercentile";
				
				public static final String PRESSURE = "pressure";
				public static final String PRESSURE_MBAR = "pressureHPAOrMBAR";
				public static final String PRESSURE_ALTITUDE = "pressureAltitude";

				public static final String LIGHT = "light";
				public static final String LIGHT_METER_VALUE = "lightMeterValue";
			}
		}

		public final static class Geo {
			public final static long LOG_RATE = 5000L;

			public final static class Keys {
				public static final String GPS_COORDS = "gps_coords";
				public static final String GPS_BEARING = "gps_bearing";
				public static final String GPS_ALTITUDE = "gps_altitude";
				public static final String GPS_SPEED = "gps_speed";
				public static final String GPS_ACCURACY = "gps_accuracy";
				public static final String GPS_TIME = "gps_time";
				
				public static final String NMEA_TIME = "nmeatime";
				public static final String NMEA_MESSAGE = "nmeamessage";
				
			}
		}
	}

	public final static class Models {
		public static final String _ID = "_id";
		public static final String _REV = "_rev";		
		
		public class LogCache {
			public final static String CACHE = "cache";
			public final static String TIME_OFFSET = "timeOffset";
		}
		
		public class INotification {
			public class Type {
				public final static int NEW_KEY = 600;
				public static final int KEY_SENT = 601;
				public static final int EXPORTED_MEDIA = 602;
				public static final int SHARED_MEDIA = 603;
			}

			public static final String ID = "notification_id";
			public static final String CLASS = "handler_message_type";
		}
		
		public class IGenealogy {
			public class OwnershipType {
				public final static int INDIVIDUAL = 400;
				public final static int ORGANIZATION = 401;
			}
		}
		
		public class IRegion {
			public final static String REGION_BOUNDS = "region_bounds";
			public static final String REGION_COORDINATES = "region_coordinates";
			public static final String REGION_DIMENSIONS = "region_dimensions";
			public static final String REGION_TIMESTAMPS = "region_timestamps";
			
			public static final String DISPLAY_TOP = "displayTop";
			public static final String DISPLAY_LEFT = "displayLeft";
			public static final String DISPLAY_WIDTH = "displayWidth";
			public static final String DISPLAY_HEIGHT = "displayHeight";
			public static final String BOUNDS = "bounds";
			public static final String INDEX = "index";
			public static final String ID = "id";
			
			public class Bounds {
				public final static String TOP = "top";
				public final static String LEFT = "left";
				public final static String WIDTH = "width";
				public final static String HEIGHT = "height";
				public final static String START_TIME = "startTime";
				public final static String END_TIME = "endTime";
				public static final String DURATION = "duration";
			}
		}
		
		public class IUser {
			public final static String PATH_TO_BASE_IMAGE = "path_to_base_image";
			public final static String AUTH_TOKEN = "auth_token";
			public final static String PASSWORD = "password";
			public static final String ALIAS = "alias";
			public static final String EMAIL = "email";
			public static final String BASE_IMAGE = "baseImage";
			public static final String CREDENTIALS = "credentials";
			public static final String SECRET = "secret";
			public static final String SECRET_AUTH_TOKEN = "secretAuthToken";
			public static final String SECRET_KEY = "secretKey";
			public static final String PGP_KEY_FINGERPRINT = "pgpKeyFingerprint";
			public static final String PUBLIC_CREDENTIALS = "publicCredentials";
			public static final String PUBLIC_KEY = "publicKey";
			public static final String BELONGS_TO_USER = "belongs_to_user";
			public static final String LANG_DEFAULT = "langDefault";
		//	public static final String ASSET_ENCRYPTION = "assetEncryption";
		}

		public class IMediaManifest {
			public class Sort {
				public final static int DATE_DESC = 0;
				public final static int DATE_ASC = 3;
				public final static int TYPE_PHOTO = 1;
				public final static int TYPE_VIDEO = 2;
				public final static int SOURCE_IOCIPHER = 4;
				public final static int SOURCE_FILE_SYSTEM = 4;
				
				//public final static int LOCATION = 0;
				public final static String IS_SHOWING = "isShowing";
			}
		}
		
		public class INotificationManifest {
			public class Sort {
				public final static int DATE_DESC = IMediaManifest.Sort.DATE_DESC;
				public static final int COMPLETED = 2;
				public final static int DATE_ASC = IMediaManifest.Sort.DATE_ASC;
			}
		}

		public class IMedia {
			public final static String _ID = "_id";
			public static final String J3M = "j3m";
			public static final String J3M_DESCRIPTOR = "j3m_descriptor";
			public static final String VERSION = "versionForExport";
			
			public class Flags {
				public final static String IS_NEW = "isNew";
			}
			
			public class ILog {
				public final static String ATTACHED_MEDIA = "attachedMedia";
				public final static String IS_CLOSED = "isClosed";
				public final static String START_TIME = "startTime";
				public final static String END_TIME = "endTime";
			}
			
			public class Data {

				public static final String SENSOR_PLAYBACK = "sensorPlayback";
				
			}
			
			public class Image {
				public static final String BITMAP = "bitmap";
			}
			
			public class Video {
				public static final String VIDEO = "video";
			}

			public class MimeType {
				public final static String IMAGE = "image/jpeg";
				public final static String VIDEO_BASE = "video/";
				public final static String VIDEO_MP4 = "video/mp4";
				public final static String VIDEO_3GPP = "video/3gpp";
				public static final String LOG = "informacam/log";
				public static final String ZIP = "application/zip";
				public static final String ICTD = "application/octet-stream";
				public static final String JSON = "application/json";
				public static final String J3M = JSON;
			}
			
			public class Assets {
			//	public final static String J3M = "informacam.j3m";
			}

			public class j3m {
				public final static String DATA = "data";
				public final static String GENEALOGY = "genealogy";
				public final static String INTENT = "intent";
				public static final String SIGNATURE = "signature";
				public static final String SIZE = "size";
				public static final String HASH = "hash";
				public static final String FILE_NAME = "file_name";
				public static final String J3M = "j3m";
			}
			
			public class TempKeys {
				public final static String IS_SELECTED = "isBatchSelected";
				public final static String SHOULD_SHOW = "shouldShow";
			}
		}

		public class ICredentials {
			public final static String PASSWORD_BLOCK = "passwordBlock";
			public final static String PASSWORD_ALIAS = "CameraV1_2";
		}

		public class IPendingConnections {

		}

		public class IConnection {
			

			public static final String DATA = "data";
			public static final String PARAMS = "params";
			public static final String _ID = "_id";
			public static final String _REV = "_rev";
			public static final String BELONGS_TO_USER = "belongs_to_user";
			public static final String BYTE_RANGE = "byte_range";
			
			public static final int MAX_TRIES = 10;
			public static final String PATH_TO_NEXT_CONNECTION_DATA = "pathToNextConnectionData";
			public static final String BYTES_TRANSFERRED = "bytes_transferred";
			public static final String BYTES_TRANSFERRED_VERIFIED = "bytes_transferred_verified";
			public static final String PROGRESS = "progress";
			public static final String PARENT = "parent";
			
			public class ResponseCodes {
				public static final int INVALID_TICKET = 48;
			}
			
			public class Type {
				public static final int NONE = 799;
				public static final int MESSAGE = 800;
				public static final int SUBMISSION = 801;
				public static final int UPLOAD = 802;
			}
			
			public class CommonParams {
				public static final String MESSAGE_TO = "message_to";
				public static final String MESSAGE_TIME = "message_time";
				public static final String MESSAGE_CONTENT = "message_content";
			}
			
			public class Routes {
				public static final String EXPORT = "export/";
				public static final String MESSAGES = "messages/";
				public static final String SUBMISSIONS = "submissions/";
				public static final String UPLOAD = "upload/";
			}
		}

		public class IDCIMEntry {
			public final static String FILE_NAME = "fileName";
			public static final String SIZE = "size";
			public static final String URI = "uri";
			public static final String TIME_CAPTURED = "timeCaptured";
			public static final String HASH = "hash";
			public static final String THUMBNAIL = "thumbnail";
			public static final String AUTHORITY = "authority";
			public static final String MEDIA_TYPE = "mediaType";
			public static final String INDEX = "index";
		}

		public class IDCIMDescriptor {
			public static final String TAG = "IDCIMDescriptor";
		}

		public class IResult {
			public final static String DATA = "data";
			public final static String REASON = "reason";
			public static final String RESPONSE_CODE = "response_code";
			public static final String CONTENT = "content";
			public static final String RESULT_CODE = "result";
			
			public class ResponseCodes {
				public static final int DOWNLOAD_ASSET = 43;
				public final static int INIT_USER = 44;
				public static final int INSTALL_ICTD = 45;
				public static final int UPLOAD_SUBMISSION = 46;
				public static final int UPLOAD_CHUNK = 47;
			}
		}

		public class ITransportStub {
			public static final int MAX_TRIES = 6;	//10;
			
			public static final String ID = "id";
			public static final String ID_HASH = "8913k5zfpo16asb08ep821wery";
			
			public static final String ASSOCIATED_NOTIFICATION = "associatedNotification";
			public static final String ORGANIZATION = "organization";
			public static final String ASSET_PATH = "assetPath";
			
			public class Methods {
				public final static int GET = 1;
				public static final int POST = 2;
				public static final int PUT = 3;
				
			}
			
			public static final String TAG = "transport_stub";
			
			public class RepositorySources {
				public final static String GOOGLE_DRIVE = "google_drive";
				public final static String GLOBALEAKS = "globaleaks";
				public final static String APP = "application";
				public final static String S3 = "s3";
				public final static String CAMERAV_EXPRESS = "camerav_express";
			}
			
			public class ResultCodes {
				public final static int FAIL = 403;
				public final static int OK = 200;
			}
			
			public class CallbackCodes {
				public final static int UPDATE_ORGANIZATION_HAS_KEY = 100;
			}
			
			public class Globaleaks {
				public final static String TAG = RepositorySources.GLOBALEAKS;
			}
			
			public class S3 {
				public final static String TAG = RepositorySources.S3;
			}
			
			public class GoogleDrive {
				public final static String TAG = RepositorySources.GOOGLE_DRIVE;
				public static final String SCOPE = "oauth2:https://www.googleapis.com/auth/drive.file";
				
				public class Urls {
					public final static String UPLOAD = "https://www.googleapis.com/upload/drive/v2/files?uploadType=multipart";
					public final static String SHARE = "https://www.googleapis.com/drive/v2/files/%s/permissions";
					
				}
				
				public class Permissions {
					public final static String USER = "user";
				}
				
				public class Roles {
					public final static String WRITER = "writer";
				}
			}
		}

		public class IIdentity {
			public final static String SOURCE = "source";
			public static final String CREDENTIALS = "credentials";
		}

		public class IOrganization {
			public static final String ORGANIZATION_DETAILS = "organizationDetails";
			public static final String ORGANIZATION_ICON = "organizationIcon";
			public static final String ORGANIZATION_NAME = "organizationName";
			public static final String ORGANIZATION_FINGERPRINT = "organizationFingerprint";
			public static final String PUBLIC_KEY = "publicKey";
			public static final String FORMS = "forms";
			public static final String REPOSITORIES = "repositories";
			public static final String APPLICATION_SIGNATURE = "applicationSignature";
			public static final String PACKAGE_NAME = "packageName";
		}
	}

	public final static class IManifest {
		public final static String USER = "informacam_manifest";
		public static final String PREF = "informacam_preferences";		
		public final static String DCIM = "dcimDescriptor";
		public final static String MEDIA = "mediaManifest";
		public static final String FORMS = "installedForms";
		public static final String ORGS = "installedOrganizations";
		public static final String KEY_STORE_MANIFEST = "keystoreManifest";
		public static final String KEY_STORE = "keystore.jks";
		public static final String CACHES = "informaCaches";
		public static final String NOTIFICATIONS = "notificationsManifest";
		public static final String DEX = "dexDump";
		public static final String LANG = "languageMap";
		public static final String TRANSPORT = "transportManifest";
		public static final String ANON = "userAnon";
	}

	public final static class App {
		public final static String LOG = "InformaMain";

		public static final class Camera {
			public final static String LOG = "InformaCamera";
			public final static String TYPE = "cameraType";

			public static final class Type {
				public final static int CAMERA = 500;
				public final static int CAMCORDER = 501;
				public final static int USERCONTROLLED = 502;
				public final static int SECURE_CAMERA = 503;
				public final static int SECURE_CAMCORDER = 504;
			}

			public static final class Intents {
				public final static String CAMERA = MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA;
				public final static String CAMCORDER = MediaStore.INTENT_ACTION_VIDEO_CAMERA;
				public final static String SECURE_CAMERA =  "info.guardianproject.action.SECURE_STILL_IMAGE_CAMERA";
				public final static String SECURE_CAMCORDER =  "info.guardianproject.action.SECURE_VIDEO_CAMERA";
			//	public final static String CAMERA_SIMPLE = MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA;
			}

			public static final class Authority {
				public final static Uri CAMERA = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				public final static Uri CAMCORDER = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			}

			public static final String TAG = "InformaCam.Camera";

			public final static List<String> SUPPORTED;
			static {
				List<String> supported = new Vector<String>();
				supported.add("org.witness.informacam");
				supported.add("com.sec.android.app.camera");
				supported.add("com.android.camera");
				supported.add("com.google.android.gallery3d");
				supported.add("com.google.android.GoogleCamera");
				supported.add("com.motorola.camera");
				SUPPORTED = Collections.unmodifiableList(supported);
			}
		}

		public final static class Transport {
			public final static String LOG = "InformaTransport";
			
			public final static class Results {
				public final static String OK_BUT_FAIL = "500: Internal Server Error";
				public final static String OK = "200";
				public final static String[] FAIL = {"404", "500"};
			}
		}

		public final static class Background {
			public final static String LOG = "InformaBackground";
		}
		
		public final static class Forms {
			public static final String FREE_AUDIO = "iWitness Free Audio Annotation";
			public static final String FREE_TEXT = "iWitness Free Text Annotations";
		}
		
		public final static class Storage {
			public final static String LOG = "InformaStorage";
			public static final String ROOT = "informaCamIOCipher";
			public static final String IOCIPHER = "ic_data.db";
			public static final String DUMP = "informaCam";

			//public static final String EXTERNAL_DIR = "/sdcard/InformaCam";//Environment.getExternalStorageDirectory().getAbsolutePath() + "/InformaCam";
			public static final String FORM_ROOT = "forms";
			public static final String ORGS_ROOT = "organizations";
			public static final String LOG_DUMP = "iLogs";
			public static final String ATTACHED_MEDIA = "attachedMedia";
			//public static final String DCIM = "/storage/extSdCard/DCIM/Camera";

            // external dirs should not be hard-coded (harlo 10/7/15)
            public static final String EXTERNAL_DIR = String.format("%s/CameraV", Environment.getExternalStorageDirectory().getAbsolutePath());
            public static final String DCIM = String.format("%s/Camera", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());

			public final static class Type {

				public static final int INTERNAL_STORAGE = 200;
				public static final int IOCIPHER = 201;
				public static final int APPLICATION_ASSET = 202;
				public static final int CONTENT_RESOLVER = 203;
				public static final int FILE_SYSTEM = 204;

			}
			
			public final static class Intake {
				public final static String TAG = "dcimIntake";
			}
			
			public final static class ICTD {
				public final static List<String> ZIP_OMITABLES;
				static {
					List<String> zip_omitables = new ArrayList<String>();
					zip_omitables.add("__MACOSX");
					zip_omitables.add("DS_Store");
					ZIP_OMITABLES = Collections.unmodifiableList(zip_omitables);
				}
			}
		}

		public final static class Informa {
			public final static String LOG = "InformaCore";
		}

		public final static class Crypto {
			public final static String LOG = "InformaCrypto";

			public final static byte[] REGION_SALT = {(byte) 0xC4, (byte) 0xE2, (byte) 0xA4, 
				(byte) 0xF2, (byte) 0xEA, (byte) 0xA0, (byte) 0xBE, (byte) 0xF7};
			public final static byte[] FORM_SALT = {(byte) 0x70, (byte) 0xB4, (byte) 0xEE,
				(byte) 0x9B, (byte) 0xD3, (byte) 0x80, (byte) 0xEC, (byte) 0x74};

			public final static class Signatures {
				public final static class Keys {
					public final static String SIGNATURE = "dataSignature";
				}
			}
		}

		public final static class ImageCapture {
			public final static String LOG = "InformaCapture";
			public final static int ROUTE = Codes.Routes.IMAGE_CAPTURE;
		}
	}
}
