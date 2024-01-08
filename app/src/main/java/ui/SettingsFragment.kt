package ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.iessochoa.radwaneabdessamie.practica5.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val buildVersion: Preference? = findPreference("buildVersion")
        val telefonoContacto : Preference? = findPreference("telefonoContacto")
//definimos la accion para la preferencia
        buildVersion?.setOnPreferenceClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://portal.edu.gva.es/03013224/"))
            )
            //hay que devolver booleano para indicar si se acepta el cambio o  no
            false
        }

        telefonoContacto?.setOnPreferenceClickListener {
            startActivity(
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:966912260"))
            )
            false
        }
    }
}