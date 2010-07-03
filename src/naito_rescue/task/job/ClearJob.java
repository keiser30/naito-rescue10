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
		
		NAITOPoliceForce me = (NAITOPoliceForce)owner;

		//自分がターゲットエリアにいないなら、まずそこへ向けて移動する
		if(owner.getLocation().getID().getValue() != target.getID().getValue()){
			
			
			owner.move(target);
			return;
		}
		//今いるところから啓開可能な閉塞を取得
		Blockade b = null;
		if(owner.getLocation() instanceof Area){
			b = owner.getTargetBlockade();
		}
		if(b != null){
			//啓開可能な閉塞があるならそこを啓開する
			
			
			owner.clear(b.getID());
		}else if(owner.getLocation().getID().getValue() == target.getID().getValue() &&
		         target.isBlockadesDefined()){
		    //ターゲットとして設定してあるエリアに存在する閉塞へ向けて移動する
			
		   	Blockade b_ = owner.getTargetBlockade(target, Integer.MAX_VALUE);
			if(b_ != null){
				
				owner.move(target, b_.getX(), b_.getY());
			}
		}
	}

	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		

		if(blockades == null) illegal = true;
		if(illegal) return true;
		
		return !target.isBlockadesDefined();
	}
}
