package com.example.gametophelper.shared.api

import android.content.Context
import com.example.gametophelper.shared.auth.SessionManager
import com.example.gametophelper.shared.models.Lesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeacherApiClient(private val context: Context? = null) {

    private fun getSessionManager(): SessionManager? {
        return context?.let { SessionManager(it) }
    }

    suspend fun login(login: String, password: String): Boolean = withContext(Dispatchers.IO) {
        // Просто проверяем, можем ли получить расписание
        val provider = OmniTokenProvider(context!!)
        val html = provider.getScheduleFromWebView(login, password)
        val success = html != null
        if (success) {
            // Сохраняем логин/пароль для будущих запросов
            getSessionManager()?.saveUser(
                login = login,
                password = password,
                token = "teacher_webview",
                userType = null
            )
            println("✅ Успешный вход через WebView")
        }
        success
    }

    suspend fun getSchedule(week: Int = 0): List<Lesson> = withContext(Dispatchers.IO) {
        try {
            val login = getSessionManager()?.getCurrentUser()?.login
            val password = getSessionManager()?.getCurrentUser()?.password

            if (login == null || password == null) {
                println("❌ Нет сохранённых данных для входа")
                return@withContext emptyList()
            }

            println("========== ЗАГРУЗКА РАСПИСАНИЯ ПРЕПОДАВАТЕЛЯ ЧЕРЕЗ WEBVIEW ==========")

            val provider = OmniTokenProvider(context!!)
            val html = provider.getScheduleFromWebView(login, password)

            if (html != null) {
                println("📦 HTML получен (${html.length} символов)")
                parseScheduleFromHtml(html)
            } else {
                println("❌ Не удалось получить HTML из WebView")
                emptyList()
            }

        } catch (e: Exception) {
            println("❌ Ошибка: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseScheduleFromHtml(html: String): List<Lesson> {
        val lessons = mutableListOf<Lesson>()

        try {
            // Ищем таблицу с расписанием
            val tableStart = html.indexOf("<table class=\"bordered schedule_table\"")
            if (tableStart == -1) {
                println("❌ Таблица schedule_table не найдена в HTML")
                // Пробуем найти любую таблицу с классом bordered
                val anyTable = html.indexOf("<table class=\"bordered")
                if (anyTable == -1) {
                    println("❌ Вообще нет таблиц bordered в HTML")
                    return emptyList()
                }
            }

            // Ищем tbody
            val tbodyStart = html.indexOf("<tbody>", tableStart)
            val tbodyEnd = html.indexOf("</tbody>", tbodyStart)
            if (tbodyStart == -1 || tbodyEnd == -1) {
                println("❌ tbody не найден")
                return emptyList()
            }

            val tbody = html.substring(tbodyStart, tbodyEnd + 8)

            // Парсим строки таблицы
            val rowRegex = "<tr[^>]*>(.*?)</tr>".toRegex(RegexOption.DOT_MATCHES_ALL)
            val cellRegex = "<td[^>]*>(.*?)</td>".toRegex(RegexOption.DOT_MATCHES_ALL)
            val pRegex = "<p[^>]*class=\"([^\"]*)\"[^>]*>(.*?)</p>".toRegex(RegexOption.DOT_MATCHES_ALL)
            val spanRegex = "<span[^>]*class=\"([^\"]*)\"[^>]*>(.*?)</span>".toRegex(RegexOption.DOT_MATCHES_ALL)

            val rows = rowRegex.findAll(tbody).toList()
            println("📊 Найдено строк в tbody: ${rows.size}")

            for (row in rows) {
                val rowHtml = row.groupValues[1]
                val cells = cellRegex.findAll(rowHtml).toList()

                if (cells.isEmpty()) continue

                // Первая ячейка — номер пары
                val firstCell = cells[0].groupValues[1]
                val pairNumber = spanRegex.find(firstCell)?.groupValues?.getOrNull(2)?.trim() ?: ""

                // Остальные ячейки — дни недели
                for (i in 1 until cells.size) {
                    val cellHtml = cells[i].groupValues[1]

                    // Ищем p с классом subject
                    val subjectMatch = pRegex.findAll(cellHtml).find { it.groupValues[1] == "subject ng-binding" }
                    val subject = subjectMatch?.groupValues?.getOrNull(2)?.trim() ?: ""

                    // Ищем p с классом group
                    val groupMatch = pRegex.findAll(cellHtml).find { it.groupValues[1] == "group ng-binding" }
                    val group = groupMatch?.groupValues?.getOrNull(2)?.trim() ?: ""

                    // Ищем p с классом auditory
                    val auditoryMatch = pRegex.findAll(cellHtml).find { it.groupValues[1] == "auditory ng-binding" }
                    val room = auditoryMatch?.groupValues?.getOrNull(2)?.trim() ?: ""

                    // Ищем p с классом time_start_end
                    val timeMatch = pRegex.findAll(cellHtml).find { it.groupValues[1] == "time_start_end ng-binding" }
                    val timeStartEnd = timeMatch?.groupValues?.getOrNull(2)?.trim() ?: ""

                    if (subject.isNotEmpty()) {
                        val timeParts = timeStartEnd.split("-").map { it.trim() }
                        val timeStart = timeParts.getOrElse(0) { "" }
                        val timeEnd = timeParts.getOrElse(1) { "" }

                        lessons.add(
                            Lesson(
                                subject = subject,
                                timeStart = timeStart,
                                timeEnd = timeEnd,
                                teacher = group,
                                room = room
                            )
                        )
                    }
                }
            }

            // Удаляем дубликаты
            val unique = lessons.distinctBy { "${it.subject}_${it.timeStart}_${it.room}" }
            println("✅ Всего спарсено уроков: ${unique.size}")

            return unique

        } catch (e: Exception) {
            println("❌ Ошибка парсинга HTML: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }
}