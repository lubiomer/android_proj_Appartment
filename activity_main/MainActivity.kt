package activity_main

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var taskEditText: EditText
    private lateinit var addButton: Button
    private lateinit var taskListView: ListView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskEditText = findViewById(R.id.taskEditText)
        addButton = findViewById(R.id.addButton)
        taskListView = findViewById(R.id.taskListView)
        taskAdapter = TaskAdapter(this, taskList)
        taskListView.adapter = taskAdapter

        addButton.setOnClickListener {
            val task = taskEditText.text.toString().trim()
            if (task.isNotEmpty()) {
                taskList.add(task)
                taskAdapter.notifyDataSetChanged()
                taskEditText.text.clear()
            } else {
                Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        taskListView.setOnItemClickListener { parent, view, position, id ->
            val task = taskList[position]
            Toast.makeText(this, "Clicked: $task", Toast.LENGTH_SHORT).show()
        }

        taskListView.setOnItemLongClickListener { parent, view, position, id ->
            taskList.removeAt(position)
            taskAdapter.notifyDataSetChanged()
            true
        }
    }
}
