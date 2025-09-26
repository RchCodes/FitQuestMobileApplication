package com.example.fitquest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Quest {

	private final Dialog dialog;
	private final Context context;
	private final QuestAdapter adapter;

	private QuestCategory currentCategory = QuestCategory.DAILY;

	public Quest(Context context) {
		this.context = context;

		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.quest, null);

		dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(view);
		dialog.setCancelable(true);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

		RecyclerView recyclerView = view.findViewById(R.id.quest_recycler);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));

		// Use public QuestManager method
		List<QuestModel> dailyQuests = QuestManager.getByCategory(context, QuestCategory.DAILY);
		adapter = new QuestAdapter(context, dailyQuests);
		recyclerView.setAdapter(adapter);

		setupTabButtons(view);
		showQuests(currentCategory);
	}

	private void setupTabButtons(View view) {
		Button dailyButton = view.findViewById(R.id.daily_button);
		Button weeklyButton = view.findViewById(R.id.weekly_button);
		Button monthlyButton = view.findViewById(R.id.monthly_button);

		dailyButton.setOnClickListener(v -> {
			currentCategory = QuestCategory.DAILY;
			showQuests(currentCategory);
		});

		weeklyButton.setOnClickListener(v -> {
			currentCategory = QuestCategory.WEEKLY;
			showQuests(currentCategory);
		});

		monthlyButton.setOnClickListener(v -> {
			currentCategory = QuestCategory.MONTHLY;
			showQuests(currentCategory);
		});
	}

	private void showQuests(QuestCategory category) {
		// Use public QuestManager method
		List<QuestModel> quests = QuestManager.getByCategory(context, category);
		adapter.updateQuests(quests);
	}

	public void show() {
		// Use public QuestManager method
		QuestManager.resetQuestsIfNeeded(context);
		showQuests(currentCategory);
		dialog.show();
	}

	public void dismiss() {
		dialog.dismiss();
	}
}
