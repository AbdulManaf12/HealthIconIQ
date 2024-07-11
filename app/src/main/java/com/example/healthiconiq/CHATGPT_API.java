package com.example.healthiconiq;
import android.net.Uri;

public class CHATGPT_API{

    public static String getData(Uri imageUri, String language) {
        switch (language) {
            case "English":
                return "Umbrella with a plus sign: Typically represents health protection or insurance.";
            case "اردو Urdu":
                return "چھتری جس کے ساتھ پلس کا نشان: عموماً صحت کے تحفظ یا بیمہ کی نمائندگی کرتا ہے";
            case "سنڌي Sindhi":
                return "چھتري سان گڏ پلس جو نشان: عام طور تي صحت جي حفاظت يا انشورنس جي نمائندگي ڪري ٿو";
            default:
                return "Language not supported";
        }
    }
}
