package com.ehedgehog.screens.help

import com.ehedgehog.base.BaseManager

class HelpManager: BaseManager() {

    fun getHelpMessage(): String {
        return """
            *🧑‍💻 Команды бота:*
            
            *• /start* – начальная страница \(в лс\)
            *• /gmprofile* – ваш профиль
            *• /immunities* – список игроков с активным иммунитетом
            *• /gift* _<immunity/unwarn/кол\-во валюты\>_ – подарить другому пользователю иммунитет/анварн/валюту
            *• /random* _<макс\. значение\> <кол\-во чисел\>_ – генератор случайных чисел
            *• /gmhelp* – список команд

            *• /rating* – рейтинг очков события
            *• /points* – посмотреть свои очки
        """.trimIndent()
    }

    fun getAdminHelpMessage(): String {
        return getHelpMessage().plus("\n\n").plus("""
            *😎 Админские команды:*
            
            *• /status* _<0\-2\>_ – изменить статус пользователя \(игрок/админ/старший админ\)
            *• /admwarn* _<id/username/reply\> <причина\>_ – варн админу \(название скорее всего временное\)
            *• /admunwarn* _<id/username/reply\> <кол\-во\>_ – снять варн админу \(тоже временное\)
            *• /give\_immun* _<id/username/reply\> <кол\-во\>_ – выдать иммунитет
            *• /give\_unwarn* _<id/username/reply\> <кол\-во\>_ – выдать анварн
            *• /give\_balance* _<id/username/reply\> <кол\-во\>_ – выдать валюту \(вероятно временное\)
            *• /help\_admin* – список команд для админов

            *• /start\_event* _<emoji\> <что выдаем\>_ – запуск ивента
            *• /stop\_event* – остановка ивента
            *• /reward* _<кол\-во\>_ – выдать очки \(reply\)
            *• /take* _<кол\-во\>_ – забрать очки \(reply\)
            *• /clear\_rating* – обнулить очки всех пользователей
        """.trimIndent())
    }

}
