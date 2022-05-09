/**
 *
 */
package it.unibo.ai.didattica.competition.tablut.logreader

import it.unibo.ai.didattica.competition.tablut.domain.State.Turn
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Andrea Galassi
 */
object LogReader {
    /**
     */
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val wins: MutableMap<String, Int> = HashMap()
        val draws: MutableMap<String, Int> = HashMap()
        val losses: MutableMap<String, Int> = HashMap()
        val winmoves: MutableMap<String, Int> = HashMap()
        val lossmoves: MutableMap<String, Int> = HashMap()
        val captures: MutableMap<String, Int> = HashMap()
        val captured: MutableMap<String, Int> = HashMap()
        val games: MutableMap<String, Int> = HashMap()
        val moves: MutableMap<String, Int> = HashMap()
        val game_out_file = File("games.txt")
        if (!game_out_file.exists()) {
            game_out_file.createNewFile()
        }
        val game_out = PrintWriter(game_out_file)
        game_out.write("White\tBlack\tEnding\tMoves\tWhite Captured\tBlack Captured\tWhite moves\tBlack moves\n\n")
        val players_out_file = File("players.txt")
        if (!players_out_file.exists()) {
            players_out_file.createNewFile()
        }
        val players_out = PrintWriter(players_out_file)
        //players_out.write(/* !!! Hit visitElement for element type: class org.jetbrains.kotlin.nj2k.tree.JKErrorExpression !!! */)
        try {
            Files.list(Paths.get("logs").toAbsolutePath()).use { path_stream ->
                val path_list = path_stream.filter { path: Path? -> Files.isRegularFile(path) }
                    .map { obj: Path -> obj.toString() }.toList()
                for (file_path in path_list) {
                    if (file_path.contains("_vs_")) {
                        val file = File(file_path)
                        val br = BufferedReader(FileReader(file))
                        var whiteP = "whiteP"
                        var blackP = "blackP"
                        var blackcaptured = 0
                        var whitecaptured = 0
                        var turn_counter = 0
                        var ending: Turn? = null
                        var line: String
                        while (br.readLine().also { line = it } != null) {
                            line = String(line.toByteArray(), StandardCharsets.UTF_8)
                            if (line.contains("Players")) {
                                println(line)
                                var splits = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val len = splits.size
                                line = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[len - 1]
                                splits = line.split("vs".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                whiteP = splits[0].replace("_".toRegex(), "").replace("\t".toRegex(), "")
                                blackP = splits[1].replace("_".toRegex(), "").replace("\t".toRegex(), "")

                                // TODO: this is unnecessary if the server writes
                                // things
                                // properly
                                val temp = StringBuilder()
                                var i = 0
                                while (i < blackP.length && i < 10) {
                                    val c = blackP[i]
                                    if (Character.isAlphabetic(c.code) || Character.isDigit(c)) temp.append(c)
                                    i++
                                }
                                blackP = temp.toString()
                            } else if (line.contains("Turn") && !line.contains("checkMove")) turn_counter++ else if (line.contains(
                                    "bianca rimossa"
                                ) && !line.contains("checkMove")
                            ) whitecaptured++ else if (line.contains("nera rimossa") && !line.contains("checkMove")) blackcaptured++ else if (line.contains(
                                    "D"
                                ) && !line.contains("checkMove")
                            ) ending = Turn.DRAW else if (line.contains("BW") && !line.contains("checkMove")) ending =
                                Turn.BLACKWIN else if (line.contains("WW") && !line.contains("checkMove")) ending =
                                Turn.WHITEWIN else if (line.contains("W") && !line.contains("checkMove")) ending =
                                Turn.BLACKWIN else if (line.contains("B") && !line.contains("checkMove")) ending =
                                Turn.WHITEWIN
                        }
                        val black_turns = turn_counter / 2
                        val white_turns = turn_counter / 2 + turn_counter % 2
                        for (map in arrayOf(
                            wins, draws, losses, winmoves, lossmoves, captures,
                            moves, games, captured
                        )) {
                            if (!map.containsKey(whiteP)) {
                                map[whiteP] = 0
                            }
                            if (!map.containsKey(blackP)) {
                                map[blackP] = 0
                            }
                        }
                        if (ending != null) {
                            captures[whiteP] = captures[whiteP]!! + blackcaptured
                            captured[blackP] = captured[blackP]!! + blackcaptured
                            captures[blackP] = captures[blackP]!! + whitecaptured
                            captured[whiteP] = captured[whiteP]!! + whitecaptured
                            games[blackP] = games[blackP]!! + 1
                            games[whiteP] = games[whiteP]!! + 1
                            moves[blackP] = moves[blackP]!! + black_turns
                            moves[whiteP] = moves[whiteP]!! + white_turns
                            when (ending) {
                                Turn.DRAW -> {
                                    draws[whiteP] = draws[whiteP]!! + 1
                                    draws[blackP] = draws[blackP]!! + 1
                                }
                                Turn.BLACKWIN -> {
                                    losses[whiteP] = losses[whiteP]!! + 1
                                    wins[blackP] = wins[blackP]!! + 1
                                    lossmoves[whiteP] = lossmoves[whiteP]!! + white_turns
                                    winmoves[blackP] = winmoves[blackP]!! + black_turns
                                }
                                Turn.WHITEWIN -> {
                                    wins[whiteP] = wins[whiteP]!! + 1
                                    losses[blackP] = losses[blackP]!! + 1
                                    winmoves[whiteP] = winmoves[whiteP]!! + white_turns
                                    lossmoves[blackP] = lossmoves[blackP]!! + black_turns
                                }
                                else -> {}
                            }
                            game_out.write(
                                """$whiteP	$blackP	$ending	$turn_counter	$whitecaptured	$blackcaptured	$white_turns	$black_turns
"""
                            )
                        } else game_out.write("ERROR IN $whiteP vs $blackP\n")
                        game_out.flush()
                        br.close()
                    }
                }
                game_out.close()
                for (name in wins.keys) {
                    val num_losses = losses[name]!!
                    var norm_loss_moves = 0
                    if (num_losses > 0) norm_loss_moves = lossmoves[name]!! / num_losses
                    val num_wins = wins[name]!!
                    var norm_win_moves = 0
                    if (num_wins > 0) norm_win_moves = winmoves[name]!! / num_wins
                    players_out.write(
                        (name + "\t" + (wins[name]!! * 3 + draws[name]!!) + "\t" + wins[name] + "\t"
                                + losses[name] + "\t" + draws[name] + "\t" + captures[name] + "\t"
                                + captured[name] + "\t" + (norm_loss_moves - norm_win_moves) + "\t" + norm_win_moves
                                + "\t" + norm_loss_moves + "\t" + moves[name] + "\t") + (wins[name]!! * 3 + draws[name]!!) * 1.0 / (games[name]!! * 1.0) + "\t" + wins[name]!! * 1.0 / (games[name]!! * 1.0) + "\t" + losses[name]!! * 1.0 / (games[name]!! * 1.0) + "\t" + draws[name]!! * 1.0 / (games[name]!! * 1.0) + "\t" + captures[name]!! * 1.0 / (games[name]!! * 1.0) + "\t" + captured[name]!! * 1.0 / (games[name]!! * 1.0) + "\t" + moves[name]!! * 1.0 / (games[name]!! * 1.0) + "\n"
                    )
                }
                players_out.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}