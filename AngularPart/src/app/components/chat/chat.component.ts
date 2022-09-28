import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {MessageService} from "../../service/message.service";
import {ParticipantModel} from "../participant-creation/models/ParticipantModel";
import {ProfileService} from "../../service/profile.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";

// @ts-ignore
@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy {
  @Input()
  chat: any;

  chatHidden: boolean = false;

  currentUser: ParticipantModel | null = null;

  message: string = "";

  file: any = null;

  formControl: FormGroup = new FormGroup({
    message: new FormControl([''], [Validators.required])
  })

  constructor(private messageService: MessageService, private profileService: ProfileService) {
    if (this.chat && this.chatHidden) {
      messageService.joinChat(this.chat.id);
    }
    if (this.profileService.currentProfile) {
      this.profileService.currentProfile.asObservable().subscribe((profile) => {
        this.currentUser = profile;
      })
    }
  }

  ngOnDestroy(): void {
    this.profileService.currentProfile.unsubscribe();
  }

  ngOnInit(): void {

  }

  changeHiddenState() {
    this.chatHidden = !this.chatHidden;
  }

  uploadFile(file: File) {
    if (file) {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        this.file = reader.result;
      };
    }
    console.log('this file',this.file)
  }

  sendMessage() {
    if (this.currentUser) {
      if (this.messageContent) {
        this.messageService.sendMessage(this.messageContent, this.chat.id, this.currentUser, this.file);
        // @ts-ignore
        document.getElementById(`image-file-content-${this.chat.id}`)?.textContent = null;
      }
    }
  }

  triggerInput() {
    document.getElementById(`image-file-content-${this.chat.id}`)?.click();
  }

  get messageContent() {
    return this.formControl.get('message')?.value;
  }
}
