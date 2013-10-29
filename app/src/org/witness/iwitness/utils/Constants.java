package org.witness.iwitness.utils;

import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.models.organizations.IOrganization;

import android.net.Uri;
import android.os.Message;
import android.view.View;

public class Constants {	
	
	public interface WizardActivityListener {
		public void onLanguageSelected(String language);
		public void onLanguageConfirmed();
		public void onUsernameCreated(String username, String email, String password);
		public void onTakePhotoClicked();
	}
	
	public interface EditorActivityListener {
		public IMedia media();
		public void onMediaScanned(Uri uri);
		public void waiter(boolean show);
	}

	public interface HomeActivityListener {
		public int[] getDimensions();
		public void launchEditor(IMedia media);
		public void logoutUser();
		public void getContextualMenuFor(IOrganization organization);
		public void getContextualMenuFor(IMedia media, View anchorView);
		public void getContextualMenuFor(INotification notification);
		public void waiter(boolean show);
		public void updateData(INotification notification, Message message);
		public void updateData(IOrganization organization, Message message);
		public void setLocale(String newLocale);
		public String getLocale();
		public void launchMain();
		public void launchGallery();
		public void launchCamera();
		public void launchVideo();
	}

	public class Codes {
		public class Routes {
			public static final int HOME = 1;
			public static final int CAMERA = 2;
			public static final int EDITOR = 3;
			public static final int WIZARD = org.witness.informacam.utils.Constants.Codes.Messages.Wizard.INIT;
			public static final int LOGIN = org.witness.informacam.utils.Constants.Codes.Messages.Login.DO_LOGIN;
			public static final int LOGOUT = org.witness.informacam.utils.Constants.Codes.Messages.Login.DO_LOGOUT;
			public static final int WIPE = 4;
		}
		
		public class Adapters {
			public static final int ALL = org.witness.informacam.utils.Constants.Codes.Adapters.ALL;
			public static final int NOTIFICATIONS = org.witness.informacam.utils.Constants.Codes.Adapters.NOTIFICATIONS;
			public static final int ORGANIZATIONS = org.witness.informacam.utils.Constants.Codes.Adapters.ORGANIZATIONS;
			public static final int GALLERY_GRID = 3;
			public static final int GALLERY_LIST = 4;
		}

		public class Media {
			public static final int ORIENTATION_PORTRAIT = org.witness.informacam.utils.Constants.Codes.Media.ORIENTATION_PORTRAIT;
			public static final int ORIENTATION_LANDSCAPE = org.witness.informacam.utils.Constants.Codes.Media.ORIENTATION_LANDSCAPE;

			public static final int TYPE_IMAGE = org.witness.informacam.utils.Constants.Codes.Media.TYPE_IMAGE;
			public static final int TYPE_VIDEO = org.witness.informacam.utils.Constants.Codes.Media.TYPE_VIDEO;
			public static final int TYPE_JOURNAL = org.witness.informacam.utils.Constants.Codes.Media.TYPE_JOURNAL;
		}

		public class Extras {
			public final static String EDIT_MEDIA = "edit_media";
			public static final String SET_ORIENTATION = "set_orientation";
			public static final String CHANGE_LOCALE = org.witness.informacam.utils.Constants.Codes.Extras.CHANGE_LOCALE;
			public static final String WIZARD_SUPPLEMENT = org.witness.informacam.utils.Constants.Codes.Extras.WIZARD_SUPPLEMENT;
			public static final String MESSAGE_CODE = org.witness.informacam.utils.Constants.Codes.Extras.MESSAGE_CODE;
			public static final String RETURNED_MEDIA = org.witness.informacam.utils.Constants.Codes.Extras.RETURNED_MEDIA;
			public static final String INSTALL_NEW_KEY = org.witness.informacam.utils.Constants.Codes.Extras.INSTALL_NEW_KEY;
			public static final String LOGOUT_USER = org.witness.informacam.utils.Constants.Codes.Extras.LOGOUT_USER;
			public static final String SET_LOCALES = org.witness.informacam.utils.Constants.Codes.Extras.SET_LOCALES;
			public static final String LOCALE_PREF_KEY = org.witness.informacam.utils.Constants.Codes.Extras.LOCALE_PREF_KEY;
			public static final String CONSOLIDATE_MEDIA = org.witness.informacam.utils.Constants.Codes.Extras.CONSOLIDATE_MEDIA;
			public static final String GENERAL_FAILURE = org.witness.informacam.utils.Constants.Codes.Extras.GENERAL_FAILURE;
			public static final String NUM_PROCESSING = org.witness.informacam.utils.Constants.Codes.Extras.NUM_PROCESSING;
			public static final String NUM_COMPLETED = org.witness.informacam.utils.Constants.Codes.Extras.NUM_COMPLETED;
			public static final String GENERATING_KEY = "generating_key";
			public static final String PERFORM_WIPE = "wipe_app";
		}
	}

	public class Utils {
		public final static String LOG = "******************** iWitness : Utils ********************";
	}

	public class Preferences {
		public class Keys {
			public final static String LOCK_SCREEN_MODE = "lockScreenMode";
			public final static String ORIGINAL_IMAGE_HANDLING = "originalImageHandling";
			public static final String LANGUAGE = "iw_language";
			public final static String PANIC_ACTION = "panicAction";
			public final static String HINT_SWIPE_SHOWN = "hintSwipeShown";
			public final static String HINT_AUDIO_NOTE_SAVED_SHOWN = "hintAudioNoteSavedShown";
			public final static String HINT_PROCESSING_IMAGES_SHOWN = "hintProcessingImagesShown";
		}

		public class OriginalImageHandling {
			public final static int DELETE_ORIGINAL = 0;
			public final static int SAVE_ORIGINAL = 1;
		}
		
		public class Locales {
			public final static int DEFAULT = 0;
			public final static int EN = 1;
			public final static int FR = 2;
			public final static int ES = 3;
			public final static int AR = 4;
		}
	
	}


	public class App {
		public class Camera {
			public final static String LOG = "******************** iWitness : CameraActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.CAMERA;
		}

		public class Editor {
			public final static String LOG = "******************** iWitness : EditorActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.EDITOR;

			public class Mode {
				public static final int NONE = 0;
				public static final int DRAG = 1;
				public static final int ZOOM = 2;
				public static final int TAP = 3;
			}
			
			public class Color {
				public final static int DRAW_COLOR = 0x00000000;
				public final static int DETECTED_COLOR = 0x00000000;
				public final static int OBSCURED_COLOR = 0x00000000;
			}
			
			public class Forms {
				public static final String FREE_AUDIO = "iWitness Free Audio Annotation";
				public static final String FREE_TEXT = "iWitness Free Text Annotations";
				public static final String TAG_FORM = "iWitness v 1.0";
				
				public class FreeText {
					public static final String TAG = Forms.FREE_TEXT;
					public static final String PROMPT = "iW_free_text";
				}
				
				public class FreeAudio {
					public static final String TAG = Forms.FREE_AUDIO;
					public static final String PROMPT = "iW_free_audio";
				}
				
				public class TagForm {
					public static final String TAG = Forms.TAG_FORM;
				}
			}


			// Maximum zoom scale
			public static final float MAX_SCALE = 10f;
		}

		public class Home {
			public final static String LOG = "******************** iWitness : HomeActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.HOME;
			public static final String TAG = "iWitness.Home";

			public class Tabs {
				public class UserManagement {
					public final static String TAG = App.UserManagement.TAG;
				}

				public class Gallery {
					public final static String TAG = App.Gallery.TAG;
				}

				public class CameraChooser {
					public final static String TAG = App.CameraChooser.TAG;
				}
				
				public class SharePopup {
					public final static String TAG = "share_popup";
				}
			}
		}

		public class UserManagement {
			public final static String TAG = "user_management";
		}

		public class Gallery {
			public final static String TAG = "gallery";
		}

		public class CameraChooser {
			public final static String TAG = "camera_chooser";
		}

		public class Router {
			public final static String LOG = "******************** iWitness : Router ********************";
		}


		public class Wizard {
			public final static String LOG = "******************** iWitness : WizardActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.WIZARD;
		}

		public class Login {
			public final static String LOG = "******************** iWitness : LoginActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.LOGIN;
		}

		public class Wipe {
			public final static String LOG = "******************** iWitness : WipeActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.WIPE;
		}

		public static final String TAG = "iWitness_main";
	}
}
