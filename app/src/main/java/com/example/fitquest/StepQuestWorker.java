package com.example.fitquest;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.Tasks;

public class StepQuestWorker extends Worker {

    public StepQuestWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();

        QuestManager.resetDailyStepCounterIfNeeded(ctx);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(ctx);
        if (account == null) return Result.retry();

        try {
            DataSet dataSet = Tasks.await(
                    Fitness.getHistoryClient(ctx, account)
                            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            );

            int totalSteps = 0;
            if (!dataSet.isEmpty()) {
                totalSteps = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
            }

            QuestManager.reportSteps(ctx, totalSteps);
            return Result.success();
        } catch (Exception e) {
            Log.e("StepQuestWorker", "Error fetching steps", e);
            return Result.retry();
        }
    }



}
