package com.example.gametophelper.shared.api

import android.content.Context
import com.example.gametophelper.shared.auth.SessionManager
import com.example.gametophelper.shared.models.Lesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeacherApiClient(private val context: Context? = null) {

    private var cachedHtml: String? = null

    private fun getSessionManager(): SessionManager? {
        return context?.let { SessionManager(it) }
    }

    suspend fun login(login: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val provider = OmniTokenProvider(context!!)
        val html = provider.getScheduleFromWebView(login, password)
        cachedHtml = html
        val success = html != null
        if (success) {
            getSessionManager()?.saveUser(
                login = login, password = password,
                token = "teacher_webview", userType = null
            )
            println("✅ Успешный вход через WebView")
        }
        success
    }

    suspend fun getSchedule(week: Int = 0): List<Lesson> = withContext(Dispatchers.IO) {
        try {
            val login = getSessionManager()?.getCurrentUser()?.login
            val password = getSessionManager()?.getCurrentUser()?.password
            if (login == null || password == null) return@withContext emptyList()

            val html = if (cachedHtml != null) {
                println("♻ Кеш HTML (${cachedHtml!!.length} символов)")
                cachedHtml.also { cachedHtml = null }
            } else {
                println("========== ЗАГРУЗКА ЧЕРЕЗ WEBVIEW ==========")
                OmniTokenProvider(context!!).getScheduleFromWebView(login, password)
            }

            if (html != null) {
                println("📦 HTML (${html.length} символов)")
                parseScheduleFromHtml(html)
            } else {
                println("❌ Не удалось получить HTML")
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseScheduleFromHtml(html: String): List<Lesson> {
        val lessons = mutableListOf<Lesson>()

        try {
            val tableStart = html.indexOf("<table class=\"bordered schedule_table\"")
            if (tableStart == -1) return emptyList()

            val tbodyStart = html.indexOf("<tbody>", tableStart)
            val tbodyEnd = html.indexOf("</tbody>", tbodyStart)
            if (tbodyStart == -1 || tbodyEnd == -1) return emptyList()

            val tbody = html.substring(tbodyStart, tbodyEnd + 8)
            val rowRegex = "<tr[^>]*>(.*?)</tr>".toRegex(RegexOption.DOT_MATCHES_ALL)
            val rows = rowRegex.findAll(tbody).toList()
            println("📊 Строк: ${rows.size}")

            for (row in rows) {
                val rowHtml = row.groupValues[1]
                val cellRegex = "<td[^>]*>(.*?)</td>".toRegex(RegexOption.DOT_MATCHES_ALL)
                val cells = cellRegex.findAll(rowHtml).toList()
                if (cells.isEmpty()) continue

                for (i in 1 until cells.size) {
                    val cellHtml = cells[i].groupValues[1]
                    val pRegex = "<p[^>]*class=\"([^\"]*)\"[^>]*>(.*?)</p>".toRegex(RegexOption.DOT_MATCHES_ALL)
                    val pMatches = pRegex.findAll(cellHtml).toList()

                    var subject = ""
                    var group = ""
                    var room = ""
                    var timeStartEnd = ""

                    for (p in pMatches) {
                        val className = p.groupValues[1]
                        val content = p.groupValues[2].trim()
                            .replace("&nbsp;", " ")
                        when {
                            className.contains("subject") -> subject = content
                            className.contains("group") -> group = content
                            className.contains("auditory") -> room = content
                            className.contains("time_start_end") -> timeStartEnd = content
                        }
                    }

                    if (subject.isNotEmpty()) {
                        val timeParts = timeStartEnd.split("-").map { it.trim() }
                        lessons.add(Lesson(
                            subject = subject,
                            timeStart = timeParts.getOrElse(0) { "" },
                            timeEnd = timeParts.getOrElse(1) { "" },
                            teacher = group,
                            room = room
                        ))
                    }
                }
            }

            val unique = lessons.distinctBy { "${it.subject}_${it.timeStart}_${it.room}" }
            println("✅ Спарсено: ${unique.size}")
            return unique

        } catch (e: Exception) {
            println("❌ Ошибка: ${e.message}")
            return emptyList()
        }
    }
}