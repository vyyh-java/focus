package com.example.in.ui.adapter;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.in.R;
import com.example.in.data.entity.Task;
import com.example.in.databinding.TaskItemBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//control ui
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnTaskActionListener listener;

    public TaskAdapter(OnTaskActionListener onTaskActionListener) {
        this.listener = onTaskActionListener;
    }

    //informing listener trigger
    public interface OnTaskActionListener{
        void onTaskFocus(int position);
        void onTaskDelete(Task task);
        void onTaskChanged(Task task);
        boolean onTaskEdit(EditText etTask, int actionId, KeyEvent event);
    }

    //declare item
    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        public final TaskItemBinding binding;

        public TaskViewHolder(TaskItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    //declare layout
    @NonNull
    @Override
    public TaskAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TaskItemBinding binding = TaskItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    //bind data
    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.TaskViewHolder holder, int position) {
        Task currentTask = tasks.get(position);
        long currentId = currentTask.taskId;

        //remove text watcher
        holder.binding.ETTask.removeCallbacks(null);
        holder.binding.ETTask.setOnFocusChangeListener(null);

        if(holder.binding.ETTask.getTag() instanceof TextWatcher){
            holder.binding.ETTask.removeTextChangedListener((TextWatcher) holder.binding.ETTask.getTag());
        }

        //set data
        holder.binding.CBTask.setChecked(currentTask.isCompleted);
        holder.binding.ETTask.setText(currentTask.taskDetail);
        holder.binding.IBtnDelete.setVisibility(View.GONE);

        //set ui
        setCheckedUI(currentTask.isCompleted, holder);
        setFocusedUI(holder.binding.ETTask.isFocused(), holder);

        //set text watcher
        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                currentTask.taskDetail = editable.toString();
            }
        };
        holder.binding.ETTask.addTextChangedListener(textWatcher);
        holder.binding.ETTask.setTag(textWatcher);

        //set listener
        holder.binding.CBTask.setOnClickListener(v -> {
            boolean isChecked = holder.binding.CBTask.isChecked();
            setCheckedUI(isChecked, holder);
            currentTask.isCompleted = isChecked;
            listener.onTaskChanged(currentTask);
        });

        holder.binding.IBtnDelete.setOnClickListener(v -> {
            listener.onTaskDelete(currentTask);
        });


        holder.binding.ETTask.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                setFocusedUI(true, holder);
                listener.onTaskFocus(holder.getBindingAdapterPosition());
            }else{
                String text = holder.binding.ETTask.getText().toString();
                if(text.trim().isEmpty()){
                    listener.onTaskDelete(currentTask);
                }else{
                    holder.binding.ETTask.postDelayed(() -> {
                        if (holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION && currentId == tasks.get(holder.getBindingAdapterPosition()).taskId) {
                            if (!holder.binding.ETTask.isFocused()) {
                                setFocusedUI(false, holder);
                                listener.onTaskChanged(currentTask);
                            }
                        }
                    },200);
                }
            }
        });
        holder.binding.ETTask.setOnEditorActionListener((v, actionId, event) -> {
            return listener.onTaskEdit((EditText) v, actionId, event);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    //refresh the list
    public void setTasks(List<Task> newTasks) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return tasks.size();
            }

            @Override
            public int getNewListSize() {
                return newTasks.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                Task oldTask = tasks.get(oldItemPosition);
                Task newTask = newTasks.get(newItemPosition);
                return oldTask.taskId == newTask.taskId;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Task oldTask = tasks.get(oldItemPosition);
                Task newTask = newTasks.get(newItemPosition);
                return oldTask.isCompleted == newTask.isCompleted && Objects.equals(oldTask.taskDetail, newTask.taskDetail);
            }
        });
        this.tasks = new ArrayList<>(newTasks);
        diffResult.dispatchUpdatesTo(this);
    }

    //setCheckedUi
    public void setCheckedUI(boolean isChecked, TaskViewHolder holder){
        //line
        holder.binding.ETTask.getPaint().setStrikeThruText(isChecked);
        //color
        holder.binding.ETTask.setActivated(!isChecked);
        //
        holder.binding.CBTask.setChecked(isChecked);
    }

    //setSelectedUi
    public void setFocusedUI(boolean hasFocus, TaskViewHolder holder){
        //btnVisibility
        holder.binding.IBtnDelete.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
    }
}