package hristostefanov.minirxdemo.business.interactors

sealed class Status

object InProgress : Status()

class Failure(val message: String): Status()

object Success: Status()