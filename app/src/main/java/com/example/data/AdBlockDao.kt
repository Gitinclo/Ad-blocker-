package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AdBlockDao {
    @Query("SELECT * FROM filter_rules ORDER BY priority DESC, id ASC")
    fun getAllFilterRules(): Flow<List<FilterRuleEntity>>

    @Query("SELECT COUNT(*) FROM filter_rules")
    suspend fun getFilterRuleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilterRule(rule: FilterRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilterRules(rules: List<FilterRuleEntity>)

    @Update
    suspend fun updateFilterRule(rule: FilterRuleEntity)

    @Query("DELETE FROM filter_rules WHERE id = :id")
    suspend fun deleteFilterRule(id: Int)

    @Query("UPDATE filter_rules SET hitCount = hitCount + 1 WHERE id = :id")
    suspend fun incrementRuleHit(id: Int)

    @Query("SELECT * FROM cosmetic_rules ORDER BY id ASC")
    fun getAllCosmeticRules(): Flow<List<CosmeticRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosmeticRule(rule: CosmeticRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosmeticRules(rules: List<CosmeticRuleEntity>)

    @Update
    suspend fun updateCosmeticRule(rule: CosmeticRuleEntity)

    @Query("DELETE FROM cosmetic_rules WHERE id = :id")
    suspend fun deleteCosmeticRule(id: Int)

    @Query("SELECT * FROM whitelisted_sites ORDER BY addedAt DESC")
    fun getAllWhitelistedSites(): Flow<List<WhitelistedSiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhitelistedSite(site: WhitelistedSiteEntity)

    @Query("DELETE FROM whitelisted_sites WHERE domain = :domain")
    suspend fun removeWhitelistedSite(domain: String)

    @Query("SELECT * FROM interception_logs ORDER BY timestamp DESC LIMIT 120")
    fun getRecentLogs(): Flow<List<InterceptionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: InterceptionLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<InterceptionLogEntity>)

    @Query("DELETE FROM interception_logs")
    suspend fun clearAllLogs()
}
