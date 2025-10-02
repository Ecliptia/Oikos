package com.ecliptia.oikos.ui.features.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.BillingCycle
import com.ecliptia.oikos.data.model.Subscription
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionsState(
    val subscriptions: List<Subscription> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionsState())
    val uiState: StateFlow<SubscriptionsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSubscriptions().collect { subscriptions ->
                _uiState.value = SubscriptionsState(
                    subscriptions = subscriptions.sortedBy { it.name },
                    isLoading = false
                )
            }
        }
    }

    fun addSubscription(name: String, amount: Double, firstBillDate: Long, category: String, cycle: BillingCycle) {
        viewModelScope.launch {
            val newSubscription = Subscription(
                id = "sub_${System.currentTimeMillis()}",
                name = name,
                amount = amount,
                firstBillDate = firstBillDate,
                category = category,
                billingCycle = cycle
            )
            repository.saveSubscription(newSubscription)
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription.id)
        }
    }
}
