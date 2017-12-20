package me.proxer.app.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import me.proxer.app.R
import me.proxer.app.util.Utils
import org.threeten.bp.LocalDateTime

/**
 * @author Ruben Gees
 */
class MediaControlView(context: Context?, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val uploaderClickSubject: PublishSubject<Uploader> = PublishSubject.create()
    val translatorGroupClickSubject: PublishSubject<SimpleTranslatorGroup> = PublishSubject.create()
    val episodeSwitchSubject: PublishSubject<Int> = PublishSubject.create()
    val bookmarkSetSubject: PublishSubject<Int> = PublishSubject.create()
    val finishClickSubject: PublishSubject<Int> = PublishSubject.create()

    var textResolver: TextResourceResolver? = null
        set(value) {
            field = value

            if (value != null) {
                previous.text = value.previous()
                next.text = value.next()
                bookmarkThis.text = value.bookmarkThis()
                bookmarkNext.text = value.bookmarkNext()
            }
        }

    private val uploaderRow: ViewGroup by bindView(R.id.uploaderRow)
    private val translatorRow: ViewGroup by bindView(R.id.translatorRow)
    private val dateRow: ViewGroup by bindView(R.id.dateRow)

    private val uploaderText: TextView by bindView(R.id.uploader)
    private val translatorGroupText: TextView by bindView(R.id.translatorGroup)
    private val dateText: TextView by bindView(R.id.date)

    private val previous: Button by bindView(R.id.previous)
    private val next: Button by bindView(R.id.next)
    private val bookmarkThis: Button by bindView(R.id.bookmarkThis)
    private val bookmarkNext: Button by bindView(R.id.bookmarkNext)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_media_control, this, true)

        setUploader(null)
        setDateTime(null)
        setTranslatorGroup(null)
    }

    fun setUploader(uploader: Uploader?) = if (uploader == null) {
        uploaderRow.visibility = View.GONE

        uploaderText.setOnClickListener(null)
    } else {
        uploaderRow.visibility = View.VISIBLE
        uploaderText.text = uploader.name

        uploaderText.setOnClickListener {
            uploaderClickSubject.onNext(uploader)
        }
    }

    fun setTranslatorGroup(group: SimpleTranslatorGroup?) = if (group == null) {
        translatorRow.visibility = View.GONE

        translatorGroupText.setOnClickListener(null)
    } else {
        translatorRow.visibility = View.VISIBLE
        translatorGroupText.text = group.name

        translatorGroupText.setOnClickListener {
            translatorGroupClickSubject.onNext(group)
        }
    }

    fun setDateTime(dateTime: LocalDateTime?) = if (dateTime == null) {
        dateRow.visibility = View.GONE
    } else {
        dateRow.visibility = View.VISIBLE
        dateText.text = Utils.dateFormatter.format(dateTime)
    }

    fun setEpisodeInfo(episodeAmount: Int, currentEpisode: Int) {
        if (currentEpisode <= 1) {
            previous.visibility = View.GONE
        } else {
            previous.visibility = View.VISIBLE

            previous.setOnClickListener {
                episodeSwitchSubject.onNext(currentEpisode - 1)
            }
        }

        if (currentEpisode >= episodeAmount) {
            next.visibility = View.GONE
        } else {
            next.visibility = View.VISIBLE

            next.setOnClickListener {
                episodeSwitchSubject.onNext(currentEpisode + 1)
            }
        }

        bookmarkNext.text = when {
            currentEpisode < episodeAmount -> textResolver?.bookmarkNext()
            else -> bookmarkNext.context.getString(R.string.view_media_control_finish)
        }

        bookmarkNext.setOnClickListener {
            if (currentEpisode < episodeAmount) {
                bookmarkSetSubject.onNext(currentEpisode + 1)
            } else {
                finishClickSubject.onNext(currentEpisode)
            }
        }

        bookmarkThis.setOnClickListener {
            bookmarkSetSubject.onNext(currentEpisode)
        }
    }

    data class Uploader(val id: String, val name: String)
    data class SimpleTranslatorGroup(val id: String, val name: String)

    interface TextResourceResolver {
        fun next(): String
        fun previous(): String
        fun bookmarkThis(): String
        fun bookmarkNext(): String
    }
}
