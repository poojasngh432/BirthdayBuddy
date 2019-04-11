package com.procrastimax.birthdaybuddy.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.procrastimax.birthdaybuddy.MainActivity
import com.procrastimax.birthdaybuddy.R
import com.procrastimax.birthdaybuddy.handler.EventHandler
import com.procrastimax.birthdaybuddy.models.EventDate
import com.procrastimax.birthdaybuddy.models.OneTimeEvent
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_event_list.*
import kotlinx.android.synthetic.main.fragment_one_time_event_instance.*
import java.text.DateFormat
import java.util.*

class OneTimeEventInstanceFragment : EventInstanceFragment() {

    /**
     * isEditOneTimeEvent is a boolean flag to indicate wether this fragment is intended to edit or add an instance of OneTimeEvent
     * this is later used to fill TextEdits with existing data of an OneTimeEvent instance
     */
    var isEditOneTimeEvent = false

    /**
     * itemID is the id the one-time event has in the EventHandler - eventlist
     * In other words this id is the index of the clicked item from the EventListFragment recyclerview
     */
    var itemID = -1

    /**
     * edit_name is the TextEdit used for editing/ showing the name of the one time event
     * It is lazy initialized
     */
    val edit_name: EditText by lazy {
        view!!.findViewById<EditText>(R.id.edit_add_fragment_name_one_time_event)
    }

    /**
     * edit_date is the TextEdit used for editing/ showing the date of the one time event
     * It is lazy initialized
     */
    val edit_date: TextView by lazy {
        view!!.findViewById<TextView>(R.id.edit_add_fragment_date_one_time_event)
    }

    /**
     * edit_note is the TextEdit used for editing/ showing the note of the one time event
     * It is lazy initialized
     */
    val edit_note: EditText by lazy {
        view!!.findViewById<EditText>(R.id.edit_add_fragment_note_one_time_event)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one_time_event_instance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //retrieve fragment parameter when edited instance
        if (arguments != null) {
            isEditOneTimeEvent = true
            if (arguments!!.size() > 0) {
                itemID = (arguments!!.getInt(ITEM_ID_PARAM))
                val oneTimeEvent = EventHandler.getList()[itemID] as OneTimeEvent

                edit_date.text = EventDate.parseDateToString(
                    EventDate.dateToCurrentTimeContext(oneTimeEvent.eventDate),
                    DateFormat.FULL
                )

                edit_name.setText(oneTimeEvent.name)
                if (!oneTimeEvent.note.isNullOrBlank()) {
                    edit_note.setText(oneTimeEvent.note)
                }

                title.text = resources.getText(R.string.toolbar_title_edit_one_time_event)
                btn_fragment_one_time_event_instance_delete.visibility = Button.VISIBLE
                btn_fragment_one_time_event_instance_delete.setOnClickListener {

                    val alert_builder = AlertDialog.Builder(context)
                    alert_builder.setTitle(resources.getString(R.string.alert_dialog_title_delete_one_time_event))
                    alert_builder.setMessage(resources.getString(R.string.alert_dialog_body_message_one_time_event))

                    val one_time_event_temp = EventHandler.getList()[itemID]
                    val context_temp = context

                    // Set a positive button and its click listener on alert dialog
                    alert_builder.setPositiveButton(resources.getString(R.string.alert_dialog_accept_delete)) { _, _ ->
                        // delete one_time_event on positive button
                        Snackbar
                            .make(
                                view,
                                resources.getString(R.string.one_time_event_deleted_notification, edit_name.text),
                                Snackbar.LENGTH_LONG
                            )
                            .setAction(R.string.snackbar_undo_action_title, View.OnClickListener {
                                EventHandler.addEvent(
                                    one_time_event_temp, context_temp!!,
                                    true
                                )
                                //get last fragment in stack list, which should be eventlistfragment, so we can update the recycler view
                                val fragment =
                                    (context_temp as MainActivity).supportFragmentManager.fragments[(context_temp).supportFragmentManager.backStackEntryCount]
                                if (fragment is EventListFragment) {
                                    fragment.recyclerView.adapter!!.notifyDataSetChanged()
                                }
                            })
                            .show()

                        EventHandler.removeEventByKey(itemID, context!!, true)
                        closeBtnPressed()
                    }
                    alert_builder.setNegativeButton(R.string.alert_dialog_dismiss_delete) { dialog, _ -> dialog.dismiss() }
                    // Finally, make the alert dialog using builder
                    val dialog: AlertDialog = alert_builder.create()
                    // Display the alert dialog on app interface
                    dialog.show()
                }
            }
        } else {
            title.text = resources.getText(R.string.toolbar_title_add_one_time_event)
            btn_fragment_one_time_event_instance_delete.visibility = Button.INVISIBLE
            (context as MainActivity).progress_bar_main.visibility = ProgressBar.GONE
            edit_date.hint = resources.getString(
                R.string.one_time_event_instance_fragment_date_edit_hint,
                EventDate.parseDateToString(Calendar.getInstance().time, DateFormat.FULL)
            )
        }

        edit_date.setOnClickListener {
            showDatePickerDialog()
        }
    }

    /**
     * showDatePickerDialog shows a standard android date picker dialog
     * The choosen date in the dialog is set to the edit_date field
     */
    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()

        //set calendar to the date which is stored in the edit field, when the edit is not empty
        if (!edit_date.text.isNullOrBlank()) {
            c.time = EventDate.parseStringToDate(edit_date.text.toString(), DateFormat.FULL)
        }
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd =
            DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { view, year_, monthOfYear, dayOfMonth ->
                // Display Selected date in Toast
                c.set(Calendar.YEAR, year_)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                if (c.time.before(Calendar.getInstance().time)) {
                    Toast.makeText(
                        view.context,
                        context!!.resources.getText(R.string.error_past_one_time_event_error),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    edit_date.text = EventDate.parseDateToString(c.time, DateFormat.FULL)
                }
            }, year, month, day)
        dpd.show()
    }

    /**
     * acceptBtnPressed is a function which is called when the toolbars accept button is pressed
     */
    override fun acceptBtnPressed() {
        val name = edit_name.text.toString()
        val date = edit_date.text.toString()
        val note = edit_note.text.toString()

        if (name.isBlank() || date.isBlank()) {
            Toast.makeText(
                context,
                context!!.resources.getText(R.string.empty_fields_error_annual_event),
                Toast.LENGTH_LONG
            )
                .show()
        } else {
            val oneTimeEvent = OneTimeEvent(EventDate.parseStringToDate(date, DateFormat.FULL), name)

            if (note.isNotBlank()) {
                oneTimeEvent.note = note
            }

            //new oneTimeEvent entry, just add a new entry in map
            if (!isEditOneTimeEvent) {
                EventHandler.addEvent(oneTimeEvent, this.context!!, true)

                Snackbar
                    .make(
                        view!!,
                        context!!.resources.getString(R.string.one_time_event_added_notification, name),
                        Snackbar.LENGTH_LONG
                    )
                    .show()
                closeBtnPressed()

                //already existant oneTimeEvent entry, overwrite old entry in map
            } else {
                if (wasChangeMade(EventHandler.getList()[itemID] as OneTimeEvent)) {
                    EventHandler.changeEventAt(itemID, oneTimeEvent, context!!, true)
                    Snackbar.make(
                        view!!,
                        context!!.resources.getString(R.string.one_time_event_changed_notification, name),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                closeBtnPressed()
            }
        }
    }

    /**
     * wasChangeMade checks wether a change to the edit fields was made or not
     * This is used to avoid unnecessary operations
     * @param event: OneTimeEvent, is the comparative object to check against the TextEdit fields
     * @return Boolean, returns false if nothing has changed
     */
    private fun wasChangeMade(event: OneTimeEvent): Boolean {
        if (edit_date.text != event.dateToPrettyString(DateFormat.FULL)) return true

        if (edit_note.text.isNotBlank() && event.note == null) {
            return true
        } else {
            if (event.note != null) {
                if (edit_note.text.toString() != event.note!!) return true
            }
        }
        if (edit_name.text.toString() != event.name) return true
        //if nothing has changed return false
        return false
    }

    companion object {
        /**
         * ONE_TIME_EVENT_INSTANCE_FRAGMENT_TAG is the fragments tag as String
         */
        val ONE_TIME_EVENT_INSTANCE_FRAGMENT_TAG = "ONE_TIME_EVENT_INSTANCE"

        /**
         * newInstance returns a new instance of OneTimeEventInstanceFragment
         */
        @JvmStatic
        fun newInstance(): OneTimeEventInstanceFragment {
            return OneTimeEventInstanceFragment()
        }
    }
}