package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

/**
 * 道路啓開Job
 */
public class ClearJob extends Job
{
	Area           target;
	int            maxDistance;
	List<EntityID> blockades;

	//コンストラクタに、!target.isBlockadesDefined()なAreaを渡してはいけない
	public ClearJob(NAITOHumanoidAgent owner, StandardWorldModel model, Area target, int distance){
		super(owner,model);
		this.maxDistance = distance;
		this.target = target;

		blockades = this.target.getBlockades();
	}

	@Override
	public void doJob(){
		logger.info(" \\\\\\\\\\ ClearJob.doJob(); \\\\\\\\\\ ");
		NAITOPoliceForce me = (NAITOPoliceForce)owner;

		//自分がターゲットエリアにいないなら、まずそこへ向けて移動する
		if(me.getLocation().getID().getValue() != target.getID().getValue()){
			logger.debug("me.getLocation() != target ===> move.");
			logger.trace("(Move target = " + target + ")");
			owner.move(target);
			return;
		}
		//今いるところから啓開可能な閉塞を取得
		Blockade b = null;
		if(me.getLocation() instanceof Area){
			b = me.getTargetBlockade((Area)(me.getLocation()), maxDistance);
		}
		if(b != null){
			//啓開可能な閉塞があるならそこを啓開する
			logger.debug("閉塞発見 ===> clear.");
			logger.trace("Blockade b = " + b);
			owner.clear(b.getID());
		}else if(me.getLocation().getID().getValue() == target.getID().getValue() &&
		         target.isBlockadesDefined()){
			    //ターゲットとして設定してあるエリアに存在する閉塞へ向けて移動する
				logger.debug("閉塞はあったが啓開範囲内に存在しない ===> move.");
			   	Blockade b_ = me.getTargetBlockade(target, Integer.MAX_VALUE);
				if(b_ != null){
					logger.trace("move(" + target + ", " + b_.getX() + ", " + b_.getY());
					owner.move(target, b_.getX(), b_.getY());
				}
		}
	}

	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		logger.info(" \\\\\\\\\\ ClearJob.isFinished(); \\\\\\\\\\ ");
		logger.info("return =>" + (target.isBlockadesDefined()?"false":"true"));
		return !target.isBlockadesDefined();
	}
}
