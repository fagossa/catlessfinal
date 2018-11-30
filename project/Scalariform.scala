import com.typesafe.sbt.SbtScalariform._
object Scalariform {
  lazy val settings = Seq(
    ScalariformKeys.preferences := formattingPreferences
  )
  lazy val formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(SpacesWithinPatternBinders, true)
      .setPreference(DanglingCloseParenthesis, Force)
  }
}