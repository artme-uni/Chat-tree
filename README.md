# Chat_tree
 Network / Lab 3

Разработать приложение для узла "надежной" сети для передачи текстовых сообщений. Узлы логически объединены в дерево, каждый узел может отправлять UDP сообщения только своим непосредственным соседям. Приложение принимает в качестве параметров собственное имя узла, процент потерь, собственный порт, а также опционально IP-адрес и порт узла, с которым нужно соединиться на старте (сделать его свом соседом в дереве). Приложение, которому не был передан IP адрес и порт узла-предка, становится отдельностоящим узлом дерева. Примеры запуска:

```$ chat_node Вася -port 2001 -loss 30 # Узел с именем "Вася", потери 30%, отдельностоящий```

```$ chat_node Петя -port 2002 -loss 40 -parent 127.0.0.1:2001 # Узел с именем "Петя", потери 40%, соединяется с Васей```

Сообщение, введенное в стандартный ввод на любом из узлов сети, передается на все остальные узлы дерева и выводится в стандартный вывод на каждом узле ровно один раз. Все сообщения идентифицируются с помощью GUID. Для обеспечения "надёжности" доставка сообщений должна быть подтверждена.

Для реализации требований, каждый узел может вести учёт отправленных и полученных сообщений, однако неограниченное расходование памяти не допускается.

Важно, что переотправка сообщений вследствие потерь не должна приводить к задержкам в доставке других сообщений, и не должна блокировать работу остальных функций программы.

При поступлении любого входящего сообщения, узел генерирует случайное число от 0 до 99 включительно. Если это число строго меньше, чем заданный в параметрах процент потерь, сообщение игнорируется полностью, имитируя сетевую потерю пакета. Это необходимо для тестирования надёжности доставки сообщений.

В любой момент времени любой узел может завершить работу или стать недоступен, тогда его узлы-соседи должны через заданный в программе таймаут перестать отправлять ему сообщения, и перестать считать его соседом. Причём дерево не должно развалиться: оставшиеся узлы должны перестроить связи между собой так, чтобы вновь получилось дерево. Для этого каждый узел выбирает для себя "заместителя" среди своих соседей и сообщает его IP-адрес и порт другим соседям. Перестроение дерева должно происходить независимо от того, происходила ли в этот момент передача текстовых сообщений между этими узлами.

Для простоты мы считаем, что выходить из строя узлы могут только по очереди, так что дерево успевает восстановиться. Также допускается потеря некоторых сообщений в момент, когда дерево перестраивается.