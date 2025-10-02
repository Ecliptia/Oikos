package com.ecliptia.oikos.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.model.Frequency
import com.ecliptia.oikos.data.model.TransactionType
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: OikosRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val recurringTransactions = repository.getRecurringTransactions().firstOrNull() ?: emptyList()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        recurringTransactions.filter { it.isActive && it.nextDueDate <= today }.forEach { rt ->
            when (rt.type) {
                TransactionType.INCOME -> {
                    val income = Income(
                        id = "auto_inc_${System.currentTimeMillis()}",
                        amount = rt.amount,
                        date = Date(),
                        description = rt.name,
                        source = rt.source ?: "Recorrente"
                    )
                    repository.saveIncome(income, emptyList())
                }
                TransactionType.EXPENSE -> {
                    val expense = Expense(
                        id = "auto_exp_${System.currentTimeMillis()}",
                        amount = rt.amount,
                        date = Date(),
                        description = rt.name,
                        category = rt.category ?: "Recorrente"
                    )
                    repository.saveExpense(expense)
                }
            }

            // Update next due date and last generated date
            val updatedRt = rt.copy(
                lastGeneratedDate = Date().time,
                nextDueDate = calculateNextDueDate(rt.nextDueDate, rt.frequency)
            )
            repository.updateRecurringTransaction(updatedRt)
        }

        return Result.success()
    }

    private fun calculateNextDueDate(currentDueDate: Long, frequency: Frequency): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDueDate }
        when (frequency) {
            Frequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            Frequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            Frequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            Frequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }
        return calendar.timeInMillis
    }

    companion object {
        const val WORK_NAME = "RecurringTransactionWorker"

        fun schedule(context: Context) {
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
                1, TimeUnit.DAYS // Run once every day
            )
                .setInitialDelay(1, TimeUnit.HOURS) // Start after an hour
                .build()

            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
