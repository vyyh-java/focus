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
import com.example.in.ui.adapter.TaskAdapter;
import java.util.List;

public class TaskHelper implements TaskAdapter.OnTaskActionListener {

    private final TextView btnAdd;
    private final TaskViewModel viewModel;
    private final TaskAdapter adapter;
    private final RecyclerView rvTask;
    private final RecyclerView.LayoutManager layoutManager;
    private boolean isAdding = false;

    public <T extends LifecycleOwner & ViewModelStoreOwner> TaskHelper(View rootView, Context context, T owner) {
        this.viewModel = new ViewModelProvider(owner).get(TaskViewModel.class);
        this.layoutManager = new LinearLayoutManager(context){
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
        this.adapter = new TaskAdapter(this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (isAdding) {
                    isAdding = false;
                    //scroll to new position
                    layoutManager.scrollToPosition(positionStart);
                    //get focus
                    rvTask.post(() -> {
                        RecyclerView.ViewHolder holder = rvTask.findViewHolderForAdapterPosition(positionStart);

                        if (holder instanceof TaskAdapter.TaskViewHolder) {
                            TaskAdapter.TaskViewHolder taskHolder = (TaskAdapter.TaskViewHolder) holder;
                            taskHolder.etTask.requestFocus();
                            //rise keyboard
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if(imm != null){
                                imm.showSoftInput(taskHolder.etTask, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }
                    });
                }
            }
        });
        this.rvTask = (RecyclerView) rootView.findViewById(R.id.RVTask);
        rvTask.setLayoutManager(layoutManager);
        rvTask.setAdapter(this.adapter);
        this.btnAdd = (TextView) rootView.findViewById(R.id.TVAdd);

        //observe data change
        viewModel.getAllTasks().observe(owner, tasks -> {
            if(tasks != null){
                adapter.setTasks(tasks);
            }
        });

        //add
        btnAdd.setOnClickListener(v -> {
            if(rvTask.getVisibility() != View.VISIBLE){
                Toast.makeText(v.getContext(), "Please return to To-do list", Toast.LENGTH_SHORT).show();
            }else{
                this.isAdding = true;
                viewModel.addTask("");
            }
        });
        ViewCompat.setWindowInsetsAnimationCallback(rvTask,
            new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {
                @NonNull
                @Override
                public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                    int keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                    rvTask.setPadding(
                            rvTask.getPaddingLeft(),
                            rvTask.getPaddingTop(),
                            rvTask.getPaddingRight(),
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
            rvTask.smoothScrollToPosition(position);
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
}
