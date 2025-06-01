package fr.leomelki.loupgarou.listeners;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.wrappers.WrappedChatComponent;

import fr.leomelki.com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import fr.leomelki.loupgarou.MainLg;
import fr.leomelki.loupgarou.classes.LGPlayer;
import fr.leomelki.loupgarou.events.LGPlayerKilledEvent.Reason;

public class JoinListener implements Listener{
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		WrapperPlayServerScoreboardTeam myTeam = new WrapperPlayServerScoreboardTeam();
		myTeam.setName(p.getName());
		myTeam.setPrefix(WrappedChatComponent.fromText(""));
		myTeam.setPlayers(Arrays.asList(p.getName()));
		myTeam.setMode(0);
		myTeam.setNameTagVisibility("always");
		myTeam.setCollisionRule("never");

		boolean noSpec = p.getGameMode() != GameMode.SPECTATOR;
		for (Player receiver : Bukkit.getOnlinePlayers()) {
			for (Player teamOwner : Bukkit.getOnlinePlayers()) {
				WrapperPlayServerScoreboardTeam remove = new WrapperPlayServerScoreboardTeam();
				remove.setName(teamOwner.getName());
				remove.setMode(1); // 1 = remove team
				remove.sendPacket(receiver);
			}
		}

		for(Player player : Bukkit.getOnlinePlayers())
			if(player != p) {
				if(player.getGameMode() != GameMode.SPECTATOR)
					player.hidePlayer(p);

				WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
				team.setName(player.getName());
				team.setPrefix(WrappedChatComponent.fromText(""));
				team.setPlayers(Arrays.asList(player.getName()));
				team.setMode(0);
				team.setNameTagVisibility("always");
				team.setCollisionRule("never");

				team.sendPacket(p);
				myTeam.sendPacket(player);
			}
		p.setFoodLevel(6);
		if(e.getJoinMessage() == null || !e.getJoinMessage().equals("joinall"))
			p.getPlayer().setResourcePack("http://leomelki.fr/mcgames/ressourcepacks/v32/loup_garou.zip");
		else {
			LGPlayer lgp = LGPlayer.thePlayer(e.getPlayer());
			lgp.showView();
			lgp.join(MainLg.getInstance().getCurrentGame());
		}
		if(noSpec)
			p.setGameMode(GameMode.ADVENTURE);
		e.setJoinMessage("");
		p.removePotionEffect(PotionEffectType.JUMP);
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		p.setWalkSpeed(0.2f);
	}
	@EventHandler
	public void onResoucePack(PlayerResourcePackStatusEvent e) {
		if(e.getStatus() == Status.SUCCESSFULLY_LOADED) {
			Player p = e.getPlayer();
			LGPlayer lgp = LGPlayer.thePlayer(p);
			lgp.showView();
			lgp.join(MainLg.getInstance().getCurrentGame());
		}else if(e.getStatus() == Status.DECLINED || e.getStatus() == Status.FAILED_DOWNLOAD)
			e.getPlayer().kickPlayer(MainLg.getPrefix()+"§cIl vous faut le resourcepack pour jouer ! ("+e.getStatus()+")");
	}
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		LGPlayer lgp = LGPlayer.thePlayer(p);
		if(lgp.getGame() != null) {
			lgp.leaveChat();
			if(lgp.getRole() != null && !lgp.isDead())
				lgp.getGame().kill(lgp, Reason.DISCONNECTED, true);
			lgp.getGame().getInGame().remove(lgp);
			lgp.getGame().checkLeave();
		}
		// Supprimer la team du joueur pour tous les joueurs
		WrapperPlayServerScoreboardTeam removeTeam = new WrapperPlayServerScoreboardTeam();
		removeTeam.setName(p.getName());
		removeTeam.setMode(1); // 1 = remove team
		for(Player online : Bukkit.getOnlinePlayers()) {
			removeTeam.sendPacket(online);
		}
		LGPlayer.removePlayer(p);
		lgp.remove();
	}
	
}
