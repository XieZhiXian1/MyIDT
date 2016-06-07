#说明：每一条要执行的语句后面必需换行以 / 结束
#单行注释用(# -- //)
#请一定要注意：小心中文下的空格及换行

DROP TABLE IF EXISTS sms;
/

#sms_type ：0:all 1:inBox(无论短信还是电话进来) 2:sent(出去) 3:draft(草稿) 4:outBox(发件箱) 5:failed 6:queued
#信息表
create table sms
(
    _id           integer   primary key autoincrement,
    phone_number  text      not null,
    sms_content   text   ,
    sms_type      integer,
    read          integer DEFAULT 0,
    create_time   integer,
    sms_resource_url text,
    sms_resource_type integer,
    sms_resource_name text,
    sms_resource_time_length integer,
    sms_resource_rs_ok integer,
    target_phone_number text      not null,
    owner_phone_number text      not null,
    is_group_message integer,
    uiCause integer DEFAULT -1
);
/
#会话表，一个会话对应一条记录
create table sms_conversation
(
    _id           integer   primary key autoincrement,
    phone_number  text      not null,
    sms_content   text   ,
    sms_count     integer,
    sms_type      integer,
    read          integer DEFAULT 0,
    create_time   integer,
    sms_resource_url text,
    sms_resource_type integer,
    sms_resource_name text,
    sms_resource_time_length integer,
    sms_resource_rs_ok integer,
    target_phone_number text      not null,
    owner_phone_number text      not null,
    is_group_message integer,
    uiCause integer DEFAULT -1
);
/
#联系人表
create table contacts
(
    _id           integer   primary key autoincrement,
    phone_number  text      not null,
	sort_key      text,
    name   		  text   
);
/

#call_type ：1-未接；2-呼入；3-呼出；4-拒接；
#呼叫记录表
create table call_log
(
    _id           integer   primary key autoincrement,
    contacts_id   integer,
    name   		  text,
    phone_number  text      not null,
    call_type     integer,
    create_time   integer
);
/
#当向信息表中插入一条数据的时候，并且是收到信息的时候，new代表插入的那条新纪录，首先删除该条信息所对应的会话记录，然后再向sms_conversation中插入一条信息，这条信息最大程度上反映出最后一条数据的特征，包括：号码、内容、种类、创建时间、是否阅读、消息条数
CREATE TRIGGER insert_sms_conversation_on_sms_insert 
AFTER INSERT ON sms 
WHEN new.sms_type = 1
BEGIN  
	DELETE FROM sms_conversation WHERE ((sms_type=1 and phone_number = new.phone_number and target_phone_number = new.target_phone_number and owner_phone_number = new.owner_phone_number) or(sms_type!=1 and phone_number = new.target_phone_number and target_phone_number = new.phone_number and owner_phone_number = new.owner_phone_number) or (is_group_message = 1 and target_phone_number = new.target_phone_number));
	INSERT INTO sms_conversation(phone_number,sms_content,sms_type,create_time,read,sms_count,sms_resource_url,sms_resource_type,sms_resource_name,sms_resource_time_length,sms_resource_rs_ok,target_phone_number,owner_phone_number,is_group_message,uiCause) 
		VALUES(new.phone_number,new.sms_content,new.sms_type,new.create_time,new.read,
		(SELECT COUNT(sms._id) FROM sms WHERE sms.sms_type != 3 AND ((sms.sms_type=1 and sms.phone_number = new.phone_number and sms.target_phone_number = new.target_phone_number) or(sms.sms_type!=1 and sms.phone_number = new.target_phone_number and sms.target_phone_number = new.phone_number))),new.sms_resource_url,new.sms_resource_type,new.sms_resource_name,new.sms_resource_time_length,new.sms_resource_rs_ok,new.target_phone_number,new.owner_phone_number,new.is_group_message,new.uiCause); 
END;
/

#当向信息表中插入一条数据的时候，并且不是收到信息的时候，是譬如发送这样的状态的时候
CREATE TRIGGER update_sms_conversation_on_sms_insert 
AFTER INSERT ON sms 
WHEN new.sms_type != 1
BEGIN  
	DELETE FROM sms_conversation WHERE ((sms_type=1 and phone_number = new.target_phone_number and target_phone_number = new.phone_number and owner_phone_number = new.owner_phone_number) or(sms_type!=1 and phone_number = new.phone_number and target_phone_number = new.target_phone_number and owner_phone_number = new.owner_phone_number) or (is_group_message = 1 and target_phone_number = new.target_phone_number));
	INSERT INTO sms_conversation(phone_number,sms_content,sms_type,create_time,read,sms_count,sms_resource_url,sms_resource_type,sms_resource_name,sms_resource_time_length,sms_resource_rs_ok,target_phone_number,owner_phone_number,is_group_message,uiCause) 
		VALUES(new.phone_number,new.sms_content,new.sms_type,new.create_time,1,
		(SELECT COUNT(sms._id) FROM sms WHERE sms.sms_type != 3 AND ((sms.sms_type=1 and sms.phone_number = new.target_phone_number and sms.target_phone_number = new.phone_number) or(sms.sms_type!=1 and sms.phone_number = new.phone_number and sms.target_phone_number = new.target_phone_number))),new.sms_resource_url,new.sms_resource_type,new.sms_resource_name,new.sms_resource_time_length,new.sms_resource_rs_ok,new.target_phone_number,new.owner_phone_number,new.is_group_message,new.uiCause); 
END;
/

#当删除sms的一条记录以后，那么conversation所对应
CREATE TRIGGER update_sms_conversation_on_sms_delete 
AFTER DELETE ON sms 
BEGIN  
	UPDATE sms_conversation SET sms_count = sms_count-1 WHERE phone_number = OLD.phone_number;
END;
/
#当以上的触发器执行了以后，对count的进行判定，假如为0，那么要删除这条会话
CREATE TRIGGER sms_conversation_count_on_update 
AFTER  UPDATE OF sms_count  ON sms_conversation 
BEGIN   
	DELETE FROM sms_conversation WHERE new.sms_count = 0 AND phone_number = new.phone_number;
END;
/

