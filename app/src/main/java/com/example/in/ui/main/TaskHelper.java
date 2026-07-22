package com.example.in.ui.main;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import com.example.in.R;
import com.example.in.data.entity.Task;
import com.example.in.databinding.ActivityMainBinding;
import com.example.in.ui.adapter.TaskAdapter;
import java.util.List;

public class TaskHelper implements TaskAdapter.OnTaskActionListener {

    private final TaskViewModel viewModel;
    private final TaskAdapter adapter;
    private ActivityMainBinding binding;
    private boolean isAdding = false;

    public <T extends LifecycleOwner & ViewModelStoreOwner> TaskHelper(T owner, ActivityMainBinding binding) {
        this.binding = binding;
        this.viewModel = new ViewModelProvider(owner).get(TaskViewModel.class);
        this.adapter = new TaskAdapter(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(binding.RVTask.getContext()){
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return SNAP_TO_START;
                    }
                };
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }
        };
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (isAdding) {
                    isAdding = false;
                    //scroll to new position
                    layoutManager.scrollToPosition(positionStart);
                    //get focus
                    binding.RVTask.post(() -> {
                        RecyclerView.ViewHolder holder = binding.RVTask.findViewHolderForAdapterPosition(positionStart);

                        if (holder instanceof TaskAdapter.TaskViewHolder) {
                            TaskAdapter.TaskViewHolder taskHolder = (TaskAdapter.TaskViewHolder) holder;
                            taskHolder.binding.ETTask.requestFocus();
                            //rise keyboard
                            InputMethodManager imm = (InputMethodManager) binding.RVTask.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            if(imm != null){
                                imm.showSoftInput(taskHolder.binding.ETTask, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }
                    });
                }
            }
        });
        binding.RVTask.setLayoutManager(layoutManager);
        binding.RVTask.setAdapter(this.adapter);

        //observe data change
        viewModel.getAllTasks().observe(owner, tasks -> {
            if(tasks != null){
                adapter.setTasks(tasks);
            }
        });

        //add
        binding.TVAdd.setOnClickListener(v -> {
            if(binding.RVTask.getVisibility() != View.VISIBLE){
                Toast.makeText(v.getContext(), "Please return to To-do list", Toast.LENGTH_SHORT).show();
            }else{
                this.isAdding = true;
                viewModel.addTask("");
            }
        });
        ViewCompat.setWindowInsetsAnimationCallback(binding.RVTask,
            new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
                @NonNull
                @Override
                public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                    int keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                    binding.RVTask.setPadding(
                            binding.RVTask.getPaddingLeft(),
                            binding.RVTask.getPaddingTop(),
                            binding.RVTask.getPaddingRight(),
                            keyboardHeight
                    );
                    return insets;
                }
            }
        );

    }

    @Override
    public void onTaskFocus(int position) {
        if(position != RecyclerView.NO_POSITION){
            binding.RVTask.smoothScrollToPosition(position);
        }
    }

    //on click listener -delete
    @Override
    public void onTaskDelete(Task task) {
        viewModel.deleteTask(task);
    }

    //one text change -update
    @Override
    public void onTaskChanged(Task task) {
        viewModel.updateTask(task);
    }

    //on editor change-keyboard
    //manage enter or any action
    @Override
    public boolean onTaskEdit(EditText etTask, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
            InputMethodManager imm = (InputMethodManager) etTask.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etTask.getWindowToken(), 0);
            }
            etTask.clearFocus();
            return true;
        }
        return false;
    }

    public void release(){
        if(binding != null){
            binding = null;
        }
    }
}
