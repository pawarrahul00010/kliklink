package com.technohertz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.technohertz.common.Constant;
import com.technohertz.exception.ResourceNotFoundException;
import com.technohertz.model.GroupProfile;
import com.technohertz.model.LikedUsers;
import com.technohertz.model.MediaFiles;
import com.technohertz.model.UserContact;
import com.technohertz.model.UserRegister;
import com.technohertz.payload.UploadFileResponse;
import com.technohertz.repo.MediaFileRepo;
import com.technohertz.repo.UserRegisterRepository;
import com.technohertz.service.IGroupProfileService;
import com.technohertz.service.IUserRegisterService;
import com.technohertz.service.impl.FileStorageService;
import com.technohertz.util.CommonUtil;
import com.technohertz.util.DateUtil;
import com.technohertz.util.ResponseObject;

@RestController
@RequestMapping("/groupRest")
public class GroupProfileController {
	
	@Autowired
	private IGroupProfileService groupProfileService;

	@Autowired
	private FileController fileController;

	@Autowired
	private IUserRegisterService userRegisterService;
	
	@Autowired
	private UserRegisterRepository registerRepository;

	@Autowired
	private MediaFileRepo mediaFileRepo;

	@Autowired
	private ResponseObject response;

	@Autowired
	private DateUtil dateUtil;
	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private FileStorageService fileStorageService;

	@SuppressWarnings("unused")
	@PostMapping("/create/{userId}")
	public ResponseEntity<ResponseObject> createGroup(@RequestParam("contacts") String contacts,
			@RequestParam("file") MultipartFile file,@RequestParam("groupName") String groupName,
			@PathVariable("userId") int userId) {

		if (contacts.equals("") || contacts == null) {

			response.setError("1");
			response.setMessage("wrong userName please enter correct value");
			response.setData("[]");
			response.setStatus("FAIL");
			return ResponseEntity.ok(response);

		} else {
			
			List<UserRegister> retrivedUserList =(List<UserRegister>) userRegisterService.getAll();//get all user from database
			
			List<UserContact> contactlist = new ArrayList<UserContact>();
			
			List<String> contactList = getContactList(contacts);
			
			Map<String, UserRegister> userList = commonUtil.getContactWithDetails(contactList, retrivedUserList);
			
			UploadFileResponse uploadFileResponse= fileController.uploadFile(file, userId);
			
			
			
			
			for(String contact  : contactList) {
			
			List<GroupProfile> userCon = groupProfileService.getUserGroupdetailByUserId(contact);
			
			List<Integer> userContains = groupProfileService.getUserGroupsByUserId(contact);
			
			
			GroupProfile groupProfile = new GroupProfile();

			if(!userContains.contains(contact)) {
				UserRegister userRegister = userList.get(contact);
				
				UserContact userContact = new UserContact();
				
				userContact.setContactNumber(contact);
				userContact.setContactName(userRegister.getUserName());
				userContact.setProfilePic(userRegister.getProfile().getCurrentProfile());
				userContact.setCreateDate(dateUtil.getDate());
				contactlist.add(userContact);
				
			
			
			
			
			
			System.out.println(userCon);
			System.out.println(userContains);
			}
			
			
//			
//			for(String contactNumber : craziContact){	
//				
//				UserContact contact = new UserContact();
//				
//				if(!userContains.contains(contactNumber)) {
//					UserRegister userRegister = userList.get(contactNumber);
//					
//					contact.setContactNumber(contactNumber);
//					contact.setContactName(userRegister.getUserName());
//					contact.setProfilePic(userRegister.getProfile().getCurrentProfile());
//					contact.setCreateDate(dateUtil.getDate());
//					tosaveuserRegister.getUserContactList().add(contact);
//					}
//				else {
//					for(UserContact userContact : userCon) {
//					tosaveuserRegister.getUserContactList().add(userContact);
//					}
//				}
//			}		
//			
//			
			}
		
			
			GroupProfile groupProfile = new GroupProfile();

			/*for(String contact  : contactList) {
			
				UserRegister userRegister = userList.get(contact);
				
				UserContact userContact = new UserContact();
				
				userContact.setContactNumber(contact);
				userContact.setContactName(userRegister.getUserName());
				userContact.setProfilePic(userRegister.getProfile().getCurrentProfile());
				userContact.setCreateDate(dateUtil.getDate());
				contactlist.add(userContact);
				
				}*/
			
				groupProfile.setGroupMember(contactlist);
				groupProfile.setCurrentProfile(uploadFileResponse.getFileDownloadUri());
				groupProfile.setDisplayName(groupName);
				groupProfile.setCreatedBy(userId);
				groupProfileService.save(groupProfile);

				response.setStatus("Success");
				response.setMessage("Group Created successfully");
				response.setError("0");
				response.setData(groupProfile);

				return ResponseEntity.ok(response);

			}
		
		}

	
	@SuppressWarnings("unused")
	@PutMapping("/displayName/{id}")
	public ResponseEntity<ResponseObject> updateDisplayName(@RequestParam("displayName") String displayName,
			@PathVariable(value = "id") String profileid) throws ResourceNotFoundException {

		if (displayName.equals("") && displayName == null && profileid.equals("") && profileid == null) {

			response.setError("1");
			response.setMessage(" please enter correct value");
			response.setData("[]");
			response.setStatus("FAIL");
			return ResponseEntity.ok(response);

		} else {

			int id = 0;
			try {
				id = Integer.parseInt(profileid);
			} catch (NumberFormatException e) {

				response.setError("1");
				response.setMessage("wrong profileId please enter numeric value");
				response.setData("[]");
				response.setStatus("FAIL");
				return ResponseEntity.ok(response);

			}

			List<GroupProfile> profile = null;
			GroupProfile updateDisplayName = null;

			profile = groupProfileService.findById(id);
			if (!profile.isEmpty()) {

				profile.get(0).setDisplayName(displayName);
				profile.get(0).setGroupId(id);
				updateDisplayName = groupProfileService.save(profile.get(0));

				response.setMessage("your Display name updated successfully");
				response.setData(updateDisplayName);
				response.setError("");
				response.setStatus("success");

				return ResponseEntity.ok(response);
			} else {
				response.setMessage("user not available");
				response.setData("[]");
				response.setError("1");
				response.setStatus("fail");
				return ResponseEntity.ok(response);
			}
		}
	}

	@PostMapping("/likes/{userId}")
	public ResponseEntity<ResponseObject> totalLikes(@RequestParam("fileid") int fileid,
			@RequestParam("isLiked") boolean isLiked, @PathVariable(value = "userId") int userId) {
		MediaFiles mediaFiles = mediaFileRepo.getById(fileid);
		UserRegister userRegister = registerRepository.getOne(userId);
		LikedUsers likedUsers = new LikedUsers();
		likedUsers.setUserName(userRegister.getUserName());
		likedUsers.setMarkType(Constant.LIKE);
		mediaFiles.getLikedUsers().add(likedUsers);
		long count = 0;

		if (mediaFiles.getLikes() == null) {
			count = 0;
		} else {
			count = mediaFiles.getLikes();
		}
		if (isLiked == true) {

			count = count + 1;
			mediaFiles.setLikes(count);
			mediaFileRepo.save(mediaFiles);

			response.setError("0");
			response.setMessage("user liked successfully");
			response.setData(mediaFiles);
			response.setStatus("SUCCESS");
			return ResponseEntity.ok(response);

		} else {

			count = count - 1;
			mediaFiles.setLikes(count);
			mediaFileRepo.save(mediaFiles);

			response.setError("1");
			response.setMessage("user unliked successfully");
			response.setData("[]");
			response.setStatus("FAIL");
			return ResponseEntity.ok(response);

		}

	}

	@SuppressWarnings("unused")
	@PostMapping("/rating/{userId}")
	public ResponseEntity<ResponseObject> totalRating(@RequestParam("fileid") String userfileid,
			@RequestParam("isRated") String isRated, @RequestParam("rateCount") String rateCounts,
			@PathVariable(value = "userId") int userId) {

		if (userfileid.equals("") && userfileid == null && isRated.equals("") && isRated == null
				&& rateCounts.equals("") && rateCounts == null) {

			response.setError("1");
			response.setMessage("wrong fileid, rateCount and isRated please enter correct value");
			response.setData("[]");
			response.setStatus("FAIL");
			return ResponseEntity.ok(response);

		} else {

			int fileid = 0;
			int rateCount = 0;
			boolean isRate = false;
			try {
				isRate = Boolean.parseBoolean(isRated);
				fileid = Integer.parseInt(userfileid);
				rateCount = Integer.parseInt(rateCounts);
			} catch (Exception e) {

				response.setError("1");
				response.setMessage("wrong fileid and rateCount please enter numeric value");
				response.setData("[]");
				response.setStatus("FAIL");
				return ResponseEntity.ok(response);

			}

			MediaFiles mediaFiles = mediaFileRepo.getById(fileid);

			UserRegister userRegister = registerRepository.getOne(userId);
			LikedUsers likedUsers = new LikedUsers();
			likedUsers.setUserName(userRegister.getUserName());
			likedUsers.setMarkType(Constant.RATE);
			mediaFiles.getLikedUsers().add(likedUsers);

			// Long totalLikes=mediaFiles.getLikes();
			long rate = 0;
			System.out.println(mediaFiles.getLikes());
			if (mediaFiles.getRating() == null) {

				rate = 0;

			} else {

				rate = mediaFiles.getRating();
			}

			if (isRate == true) {
				rate = rate + rateCount;
				mediaFiles.setRating(rate);
				mediaFileRepo.save(mediaFiles);

				response.setError("0");
				response.setMessage("user rated with : " + rateCount);
				response.setData(mediaFiles);
				response.setStatus("SUCCESS");
				return ResponseEntity.ok(response);

			} else {

				mediaFiles.setRating(rate);
				mediaFileRepo.save(mediaFiles);
				response.setError("1");
				response.setMessage("rating on image is not done");
				response.setData("[]");
				response.setStatus("FAIL");
				return ResponseEntity.ok(response);
			}
		}
	}

	@SuppressWarnings("unused")
	@PutMapping("/aboutUs/{id}")
	public ResponseEntity<ResponseObject> updateStatus(@RequestParam("aboutUs") String aboutUs,
			@PathVariable(value = "id") String userid) throws ResourceNotFoundException {

		if (aboutUs.equals("") && aboutUs == null && userid.equals("") && userid == null) {

			response.setError("1");
			response.setMessage("wrong aboutUs and userid please enter correct value");
			response.setData("[]");
			response.setStatus("FAIL");
			return ResponseEntity.ok(response);

		} else {

			int id = 0;
			int rateCount = 0;
			boolean isRated = false;
			try {
				id = Integer.parseInt(userid);
			} catch (NumberFormatException e) {

				response.setError("1");
				response.setMessage("wrong fileid and rateCount please enter numeric value");
				response.setData("[]");
				response.setStatus("FAIL");
				return ResponseEntity.ok(response);

			}

			List<GroupProfile> profile = null;
			GroupProfile updateDisplayName = null;

			profile = groupProfileService.findById(id);
			if (!profile.isEmpty()) {
				profile.get(0).setAboutUser(aboutUs);
				profile.get(0).setGroupId(id);
				updateDisplayName = groupProfileService.save(profile.get(0));

				response.setMessage("your status updated successfully");
				response.setData(updateDisplayName);
				response.setError("");
				response.setStatus("success");

				return ResponseEntity.ok(response);

			} else {

				response.setMessage("user not available");
				response.setData("[]");
				response.setError("1");
				response.setStatus("fail");
				return ResponseEntity.ok(response);
			}
		}
	}

	@PutMapping("/uploadFile/{userId}")
	public ResponseEntity<ResponseObject> updateProfile(@RequestParam("file") MultipartFile file,
			@PathVariable(value = "userId") Integer userId) {
		GroupProfile groupProfile = fileStorageService.savegroupProfile(file, userId);
		MediaFiles files = mediaFileRepo.getOne(Integer
				.valueOf(String.valueOf(groupProfile.getFiles().get(groupProfile.getFiles().size() - 1).getFileId())));
		System.out.println(files);
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
				.path(String.valueOf(files.getFilePath())).toUriString();
		Object obj = new UploadFileResponse(groupProfile.getCurrentProfile(), fileDownloadUri, file.getContentType(),
				file.getSize());
		if (!file.isEmpty()||userId!=null) {
			response.setMessage("your Profile Image updated successfully");

			response.setData(obj);
			response.setError("");
			response.setStatus("success");

			return ResponseEntity.ok(response);
		} else {
			response.setMessage("your Profile Image not updated");

			response.setData("[]");
			response.setError("");
			response.setStatus("success");

			return ResponseEntity.ok(response);
		}
	}

	private List<String> getContactList(String userContactList) {
		
		List<String> contactList = new ArrayList<String>();
		String sContact[] = userContactList.split(",");
		
		for(String userContact : sContact ) {
		
			try {
				contactList.add(userContact);
			} catch (NumberFormatException e) {
				continue;
			}
		
		}
		return contactList;
	}

}
