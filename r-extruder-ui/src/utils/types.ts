import { UploadFileInfo, UploadProps } from "@progress/kendo-react-upload";

export type TBasfHeader = {
  title?: string;
  logo?: string;
  "text-color"?: string;
  styles?: string;
  environment?: string;
};

export type TBasfFooter = {
  "footer-title"?: string;
  "text-color"?: string;
  navigation?: string;
  version?: string;
  styles?: string;
  environment?: string;
  domainName?: string;
};

export interface FieldRenderPropsDropdown {
  value: string;
  onChange: (value: { value: string }) => void;
}
export interface EventDropdown {
  target: {
    value: {
      id: string;
    };
  };
}
export interface FieldRenderProps extends UploadProps {
  value: UploadFileInfo[];
  onChange: (value: { value: UploadFileInfo[] }) => void;
}
export interface UploadInputProps extends FieldRenderProps {
  handleInsertFile: (value: boolean) => void;
}
export interface SuccessNotificationProps {
  successConversion: boolean;
  onClose: (e: boolean) => void;
}

export type S3KeyDisplayProps = {
  s3Key: string | null;
  copyToClipboard: () => void;
};
export type FileWithRaw = {
  getRawFile: () => File;
};
